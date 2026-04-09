package com.iboi.relatorio.usecase

import com.iboi.financeiro.repository.DespesaRepository
import com.iboi.rebanho.api.dto.GmdJanelaDto
import com.iboi.rebanho.domain.*
import com.iboi.rebanho.repository.AnimalRepository
import com.iboi.rebanho.repository.EventoRepository
import com.iboi.relatorio.dto.*
import com.iboi.sanitario.repository.AgendamentoSanitarioRepository
import com.iboi.sanitario.domain.StatusAgendamento
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.Period
import java.util.*

@Component
class RelatorioRebanhoUseCase(
        private val animalRepository: AnimalRepository,
        private val eventoRepository: EventoRepository
) {
    fun execute(farmId: UUID): RelatorioRebanhoResponse {
        val animais = animalRepository.findByFarmId(farmId)

        val porCategoria = animais.groupingBy { it.categoria }.eachCount().mapValues { it.value.toLong() }
        val porSexo = animais.groupingBy { it.sexo }.eachCount().mapValues { it.value.toLong() }
        val porStatus = animais.groupingBy { it.status }.eachCount().mapValues { it.value.toLong() }

        val idades = animais.map {
            Period.between(it.dataNascimento, LocalDate.now()).toTotalMonths().toInt()
        }
        val idadeMedia = if (idades.isNotEmpty()) idades.average().toInt() else 0

        val pesos = animais.mapNotNull { it.pesoAtual }
        val pesoMedio = if (pesos.isNotEmpty()) {
            pesos.reduce { acc, bigDecimal -> acc.add(bigDecimal) }
                    .divide(BigDecimal(pesos.size), 2, RoundingMode.HALF_UP)
        } else null

        val gmdPorJanela = listOf(30, 60, 90).map { janela ->
            val gmds = mutableListOf<BigDecimal>()

            animais.forEach { animal ->
                val pesagens = eventoRepository.findByAnimalIdAndTipo(animal.id!!, TipoEvento.PESAGEM)
                        .filter { it.peso != null }
                        .sortedBy { it.data }

                val ultima = pesagens.lastOrNull() ?: return@forEach
                val dataCorte = ultima.data.minusDays(janela.toLong())
                val base = pesagens.filter { !it.data.isBefore(dataCorte) }.minByOrNull { it.data } ?: pesagens.firstOrNull()
                if (base == null || base.id == ultima.id) return@forEach

                val dias = java.time.temporal.ChronoUnit.DAYS.between(base.data, ultima.data)
                if (dias <= 0) return@forEach

                val variacao = ultima.peso!!.subtract(base.peso)
                gmds.add(variacao.divide(BigDecimal.valueOf(dias), 3, RoundingMode.HALF_UP))
            }

            GmdJanelaDto(
                    janelaDias = janela,
                    pesoInicial = null,
                    pesoFinal = null,
                    dataInicial = null,
                    dataFinal = null,
                    diasConsiderados = null,
                    variacaoPeso = null,
                    ganhoMedioDiario = if (gmds.isNotEmpty()) {
                        gmds.reduce(BigDecimal::add)
                                .divide(BigDecimal(gmds.size), 3, RoundingMode.HALF_UP)
                    } else null
            )
        }

        return RelatorioRebanhoResponse(
                totalAnimais = animais.size.toLong(),
                porCategoria = porCategoria,
                porSexo = porSexo,
                porStatus = porStatus,
                idadeMediaMeses = idadeMedia,
                pesoMedio = pesoMedio,
                gmdPorJanela = gmdPorJanela
        )
    }
}

@Component
class RelatorioFinanceiroUseCase(
        private val despesaRepository: DespesaRepository
) {
    fun execute(farmId: UUID, dataInicio: LocalDate, dataFim: LocalDate): RelatorioFinanceiroResponse {
        val totalDespesas = despesaRepository.sumByFarmIdAndDataBetween(farmId, dataInicio, dataFim) ?: BigDecimal.ZERO

        val porCategoria = despesaRepository.sumByFarmIdAndDataBetweenGroupByCategoria(farmId, dataInicio, dataFim)
                .map {
                    val categoria = it[0].toString()
                    val total = it[1] as BigDecimal
                    val percentual = if (totalDespesas > BigDecimal.ZERO) {
                        total.divide(totalDespesas, 4, RoundingMode.HALF_UP)
                                .multiply(BigDecimal(100))
                                .toDouble()
                    } else 0.0

                    DespesaCategoriaDto(categoria, total, percentual)
                }

        return RelatorioFinanceiroResponse(
                periodo = PeriodoDto(dataInicio, dataFim),
                totalDespesas = totalDespesas,
                despesasPorCategoria = porCategoria,
                comparativoMesAnterior = null // TODO: implementar comparativo
        )
    }
}

@Component
class HistoricoAnimalUseCase(
        private val animalRepository: AnimalRepository,
        private val eventoRepository: EventoRepository
) {
    fun execute(animalId: UUID): HistoricoAnimalResponse {
        val animal = animalRepository.findById(animalId).orElseThrow {
            IllegalArgumentException("Animal não encontrado")
        }

        val eventos = eventoRepository.findByAnimalIdOrderByDataDesc(animalId)

        val timeline = eventos.map {
            EventoTimelineDto(
                    data = it.data,
                    tipo = it.tipo,
                    descricao = it.descricao
            )
        }

        val pesagens = eventos.filter { it.tipo == TipoEvento.PESAGEM && it.peso != null }
                .map { PesoDto(it.data, it.peso!!) }

        return HistoricoAnimalResponse(
                animalId = animal.id.toString(),
                brinco = animal.brinco,
                nome = animal.nome,
                timeline = timeline,
                evolucaoPeso = pesagens,
                totalEventos = eventos.size.toLong()
        )
    }
}

@Component
class DashboardUseCase(
        private val animalRepository: AnimalRepository,
        private val eventoRepository: EventoRepository,
        private val despesaRepository: DespesaRepository,
        private val agendamentoRepository: AgendamentoSanitarioRepository
) {
    fun execute(farmId: UUID): DashboardResponse {
        val hoje = LocalDate.now()
        val inicioMes = hoje.withDayOfMonth(1)

        val totalAtivos = animalRepository.countByFarmIdAndStatus(farmId, StatusAnimal.ATIVO)

        val eventosMes = eventoRepository.findByFarmIdAndDataBetween(farmId, inicioMes, hoje)
        val nascimentosMes = eventosMes.count { it.tipo == TipoEvento.NASCIMENTO }.toLong()
        val mortesMes = eventosMes.count { it.tipo == TipoEvento.MORTE }.toLong()

        val despesasMes = despesaRepository.sumByFarmIdAndDataBetween(farmId, inicioMes, hoje) ?: BigDecimal.ZERO

        val animaisPorCategoria = animalRepository.findByFarmId(farmId)
                .groupingBy { it.categoria.toString() }
                .eachCount()
                .mapValues { it.value.toLong() }

        val eventosRecentes = eventosMes.take(5).map {
            EventoRecenteDto(
                    data = it.data,
                    tipo = it.tipo.toString(),
                    animal = it.animal.brinco,
                    descricao = it.descricao
            )
        }

        val agendamentosProximos = agendamentoRepository.findByFarmIdAndStatus(farmId, StatusAgendamento.PENDENTE)
                .take(5)
                .map {
                    AgendamentoProximoDto(
                            dataPrevista = it.dataPrevista,
                            tipo = it.itemProtocolo.tipo.toString(),
                            animal = it.animal.brinco,
                            produto = it.itemProtocolo.produto
                    )
                }

        return DashboardResponse(
                kpis = KpisDto(
                        totalAnimaisAtivos = totalAtivos,
                        nascimentosMes = nascimentosMes,
                        mortesMes = mortesMes,
                        despesasMes = despesasMes,
                        animaisPorCategoria = animaisPorCategoria
                ),
                eventosRecentes = eventosRecentes,
                agendamentosProximos = agendamentosProximos
        )
    }
}
