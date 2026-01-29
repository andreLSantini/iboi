package com.iboi.ia.service

import com.iboi.financeiro.repository.DespesaRepository
import com.iboi.ia.domain.*
import com.iboi.ia.repository.AlertaRepository
import com.iboi.identity.domain.Farm
import com.iboi.rebanho.domain.StatusAnimal
import com.iboi.rebanho.domain.TipoEvento
import com.iboi.rebanho.repository.AnimalRepository
import com.iboi.rebanho.repository.EventoRepository
import com.iboi.sanitario.domain.StatusAgendamento
import com.iboi.sanitario.repository.AgendamentoSanitarioRepository
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.*

@Service
class DetectorAlertasSanitariosService(
        private val agendamentoRepository: AgendamentoSanitarioRepository,
        private val eventoRepository: EventoRepository,
        private val animalRepository: AnimalRepository,
        private val alertaRepository: AlertaRepository
) {

    fun detectar(farm: Farm): List<Alerta> {
        val alertas = mutableListOf<Alerta>()

        // 1. Detectar vacinas atrasadas
        val vacinasAtrasadas = agendamentoRepository.findAtrasados(LocalDate.now())
                .filter { it.animal.farm.id == farm.id }

        vacinasAtrasadas.forEach { agendamento ->
            val diasAtraso = ChronoUnit.DAYS.between(agendamento.dataPrevista, LocalDate.now())
            alertas.add(
                    Alerta(
                            farm = farm,
                            tipo = TipoAlerta.VACINA_ATRASADA,
                            prioridade = when {
                                diasAtraso > 30 -> PrioridadeAlerta.CRITICA
                                diasAtraso > 15 -> PrioridadeAlerta.ALTA
                                else -> PrioridadeAlerta.MEDIA
                            },
                            titulo = "Vacina atrasada",
                            mensagem = "Animal ${agendamento.animal.brinco} está com ${agendamento.itemProtocolo.produto} atrasado há $diasAtraso dias",
                            animal = agendamento.animal,
                            recomendacao = "Aplicar ${agendamento.itemProtocolo.produto} o mais rápido possível"
                    )
            )
        }

        // 2. Detectar animais sem pesagem recente (mais de 60 dias)
        val animaisAtivos = animalRepository.findByFarmIdAndStatus(farm.id!!, StatusAnimal.ATIVO)
        animaisAtivos.forEach { animal ->
            val ultimaPesagem = eventoRepository.findByAnimalIdAndTipo(animal.id!!, TipoEvento.PESAGEM)
                    .firstOrNull()

            if (ultimaPesagem == null || ChronoUnit.DAYS.between(ultimaPesagem.data, LocalDate.now()) > 60) {
                alertas.add(
                        Alerta(
                                farm = farm,
                                tipo = TipoAlerta.SEM_PESAGEM_RECENTE,
                                prioridade = PrioridadeAlerta.MEDIA,
                                titulo = "Pesagem atrasada",
                                mensagem = "Animal ${animal.brinco} não é pesado há mais de 60 dias",
                                animal = animal,
                                recomendacao = "Realizar pesagem para acompanhar desenvolvimento"
                        )
                )
            }
        }

        return alertas
    }
}

@Service
class DetectorAlertasProdutivosService(
        private val animalRepository: AnimalRepository,
        private val eventoRepository: EventoRepository,
        private val despesaRepository: DespesaRepository,
        private val alertaRepository: AlertaRepository
) {

    fun detectar(farm: Farm): List<Alerta> {
        val alertas = mutableListOf<Alerta>()

        // 1. Detectar peso abaixo da média
        val animaisAtivos = animalRepository.findByFarmIdAndStatus(farm.id!!, StatusAnimal.ATIVO)
        val pesos = animaisAtivos.mapNotNull { it.pesoAtual }

        if (pesos.isNotEmpty()) {
            val pesoMedio = pesos.reduce { acc, peso -> acc.add(peso) }
                    .divide(BigDecimal(pesos.size), 2, RoundingMode.HALF_UP)

            animaisAtivos.forEach { animal ->
                animal.pesoAtual?.let { peso ->
                    val percentualAbaixo = peso.divide(pesoMedio, 2, RoundingMode.HALF_UP)
                            .multiply(BigDecimal(100))

                    if (percentualAbaixo < BigDecimal(70)) { // 30% abaixo da média
                        alertas.add(
                                Alerta(
                                        farm = farm,
                                        tipo = TipoAlerta.PESO_ABAIXO_MEDIA,
                                        prioridade = PrioridadeAlerta.ALTA,
                                        titulo = "Peso abaixo da média",
                                        mensagem = "Animal ${animal.brinco} está 30% abaixo do peso médio do rebanho",
                                        animal = animal,
                                        recomendacao = "Verificar saúde e ajustar alimentação"
                                )
                        )
                    }
                }
            }
        }

        // 2. Detectar mortalidade alta (mais de 5% no mês)
        val hoje = LocalDate.now()
        val inicioMes = hoje.withDayOfMonth(1)
        val mortesMes = eventoRepository.findByFarmIdAndDataBetween(farm.id, inicioMes, hoje)
                .count { it.tipo == TipoEvento.MORTE }

        val totalAnimais = animaisAtivos.size
        if (totalAnimais > 0) {
            val taxaMortalidade = (mortesMes.toDouble() / totalAnimais) * 100

            if (taxaMortalidade > 5.0) {
                alertas.add(
                        Alerta(
                                farm = farm,
                                tipo = TipoAlerta.MORTALIDADE_ALTA,
                                prioridade = PrioridadeAlerta.CRITICA,
                                titulo = "Mortalidade acima do normal",
                                mensagem = "Taxa de mortalidade está em ${String.format("%.2f", taxaMortalidade)}% neste mês (acima de 5%)",
                                recomendacao = "Consultar veterinário e revisar protocolo sanitário"
                        )
                )
            }
        }

        // 3. Detectar custos elevados (20% acima da média dos últimos 3 meses)
        val mes1Inicio = hoje.minusMonths(3).withDayOfMonth(1)
        val mes1Fim = mes1Inicio.plusMonths(1).minusDays(1)
        val mes2Inicio = hoje.minusMonths(2).withDayOfMonth(1)
        val mes2Fim = mes2Inicio.plusMonths(1).minusDays(1)
        val mes3Inicio = hoje.minusMonths(1).withDayOfMonth(1)
        val mes3Fim = mes3Inicio.plusMonths(1).minusDays(1)

        val custo1 = despesaRepository.sumByFarmIdAndDataBetween(farm.id, mes1Inicio, mes1Fim) ?: BigDecimal.ZERO
        val custo2 = despesaRepository.sumByFarmIdAndDataBetween(farm.id, mes2Inicio, mes2Fim) ?: BigDecimal.ZERO
        val custo3 = despesaRepository.sumByFarmIdAndDataBetween(farm.id, mes3Inicio, mes3Fim) ?: BigDecimal.ZERO
        val custoMesAtual = despesaRepository.sumByFarmIdAndDataBetween(farm.id, inicioMes, hoje) ?: BigDecimal.ZERO

        val custoMedio = custo1.add(custo2).add(custo3).divide(BigDecimal(3), 2, RoundingMode.HALF_UP)

        if (custoMedio > BigDecimal.ZERO && custoMesAtual > custoMedio.multiply(BigDecimal("1.2"))) {
            alertas.add(
                    Alerta(
                            farm = farm,
                            tipo = TipoAlerta.CUSTO_ELEVADO,
                            prioridade = PrioridadeAlerta.ALTA,
                            titulo = "Custos elevados",
                            mensagem = "Custos deste mês estão 20% acima da média dos últimos 3 meses",
                            recomendacao = "Revisar despesas e buscar oportunidades de economia"
                    )
            )
        }

        return alertas
    }
}
