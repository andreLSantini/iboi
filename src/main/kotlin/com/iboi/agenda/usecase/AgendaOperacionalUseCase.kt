package com.iboi.agenda.usecase

import com.iboi.agenda.api.dto.AgendaOperacionalItemDto
import com.iboi.agenda.api.dto.AgendaOperacionalResponse
import com.iboi.agenda.api.dto.AgendaOperacionalResumoDto
import com.iboi.agenda.api.dto.CategoriaAgenda
import com.iboi.agenda.api.dto.OrigemAgenda
import com.iboi.agenda.api.dto.PrioridadeAgenda
import com.iboi.agenda.api.dto.SituacaoAgenda
import com.iboi.rebanho.api.dto.AnimalResumoDto
import com.iboi.rebanho.api.dto.LoteResumoDto
import com.iboi.rebanho.domain.TipoEvento
import com.iboi.rebanho.repository.AnimalRepository
import com.iboi.rebanho.repository.EventoRepository
import com.iboi.sanitario.domain.StatusAgendamento
import com.iboi.sanitario.repository.AgendamentoSanitarioRepository
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.UUID

@Component
class AgendaOperacionalUseCase(
        private val agendamentoSanitarioRepository: AgendamentoSanitarioRepository,
        private val animalRepository: AnimalRepository,
        private val eventoRepository: EventoRepository
) {

    fun execute(farmId: UUID): AgendaOperacionalResponse {
        val hoje = LocalDate.now()
        val itens = buildList {
            addAll(buildItensSanitarios(farmId, hoje))
            addAll(buildItensPesagem(farmId, hoje))
            addAll(buildItensReproducao(farmId, hoje))
        }.distinctBy { it.id }
                .sortedWith(compareBy<AgendaOperacionalItemDto>({ it.dataPrevista }, { prioridadeRank(it.prioridade) }))

        return AgendaOperacionalResponse(
                resumo = AgendaOperacionalResumoDto(
                        total = itens.size,
                        atrasados = itens.count { it.situacao == SituacaoAgenda.ATRASADO },
                        hoje = itens.count { it.situacao == SituacaoAgenda.HOJE },
                        proximos7Dias = itens.count { it.diasParaVencimento in 1..7 }
                ),
                itens = itens
        )
    }

    private fun buildItensSanitarios(farmId: UUID, hoje: LocalDate): List<AgendaOperacionalItemDto> {
        return agendamentoSanitarioRepository.findByFarmIdAndStatus(farmId, StatusAgendamento.PENDENTE).map { agendamento ->
            val dias = ChronoUnit.DAYS.between(hoje, agendamento.dataPrevista)
            AgendaOperacionalItemDto(
                    id = "sanitario-${agendamento.id}",
                    categoria = CategoriaAgenda.SANITARIO,
                    origem = OrigemAgenda.AGENDAMENTO,
                    titulo = "${agendamento.itemProtocolo.tipo.name.replace("_", " ")}: ${agendamento.itemProtocolo.produto}",
                    descricao = "Manejo sanitario pendente no protocolo ${agendamento.itemProtocolo.protocolo.nome}.",
                    dataPrevista = agendamento.dataPrevista,
                    situacao = situacaoPara(dias),
                    prioridade = when {
                        dias < 0 -> PrioridadeAgenda.ALTA
                        dias <= 3 -> PrioridadeAgenda.ALTA
                        dias <= 7 -> PrioridadeAgenda.MEDIA
                        else -> PrioridadeAgenda.BAIXA
                    },
                    diasParaVencimento = dias,
                    animal = AnimalResumoDto(
                            id = agendamento.animal.id!!,
                            brinco = agendamento.animal.brinco,
                            nome = agendamento.animal.nome
                    ),
                    lote = agendamento.animal.lote?.let { LoteResumoDto(it.id!!, it.nome) }
            )
        }
    }

    private fun buildItensPesagem(farmId: UUID, hoje: LocalDate): List<AgendaOperacionalItemDto> {
        val ultimasPesagens = eventoRepository.findByFarmIdAndTipoOrderByDataDesc(farmId, TipoEvento.PESAGEM)
        val ultimaPesagemPorAnimal = ultimasPesagens
                .filter { it.peso != null }
                .groupBy { it.animal.id!! }
                .mapValues { (_, eventos) -> eventos.maxByOrNull { it.data }!! }

        return animalRepository.findAtivosRecentesByFarmId(farmId).mapNotNull { animal ->
            val ultimaPesagem = ultimaPesagemPorAnimal[animal.id!!]
            val dataBase = ultimaPesagem?.data ?: animal.dataEntrada ?: animal.dataNascimento
            val dataPrevista = dataBase.plusDays(30)
            val dias = ChronoUnit.DAYS.between(hoje, dataPrevista)

            if (dias > 14) {
                return@mapNotNull null
            }

            AgendaOperacionalItemDto(
                    id = "pesagem-${animal.id}",
                    categoria = CategoriaAgenda.PESAGEM,
                    origem = OrigemAgenda.HEURISTICA,
                    titulo = "Registrar pesagem de rotina",
                    descricao = if (ultimaPesagem == null) {
                        "Animal sem historico de pesagem estruturado."
                    } else {
                        "Ultima pesagem em ${ultimaPesagem.data}. Atualize a base produtiva do animal."
                    },
                    dataPrevista = dataPrevista,
                    situacao = situacaoPara(dias),
                    prioridade = when {
                        ultimaPesagem == null -> PrioridadeAgenda.ALTA
                        dias < 0 -> PrioridadeAgenda.ALTA
                        dias <= 3 -> PrioridadeAgenda.MEDIA
                        else -> PrioridadeAgenda.BAIXA
                    },
                    diasParaVencimento = dias,
                    animal = AnimalResumoDto(
                            id = animal.id!!,
                            brinco = animal.brinco,
                            nome = animal.nome
                    ),
                    lote = animal.lote?.let { LoteResumoDto(it.id!!, it.nome) }
            )
        }
    }

    private fun buildItensReproducao(farmId: UUID, hoje: LocalDate): List<AgendaOperacionalItemDto> {
        val coberturas = eventoRepository.findByFarmIdAndTipoOrderByDataDesc(farmId, TipoEvento.COBERTURA)
        val inseminacoes = eventoRepository.findByFarmIdAndTipoOrderByDataDesc(farmId, TipoEvento.INSEMINACAO)
        val diagnosticos = eventoRepository.findByFarmIdAndTipoOrderByDataDesc(farmId, TipoEvento.DIAGNOSTICO_GESTACAO)
        val partos = eventoRepository.findByFarmIdAndTipoOrderByDataDesc(farmId, TipoEvento.PARTO)
        val eventosBase = (coberturas + inseminacoes)
                .filter { it.animal.sexo.name == "FEMEA" }
                .groupBy { it.animal.id!! }
                .mapValues { (_, eventos) -> eventos.maxByOrNull { it.data }!! }

        return eventosBase.values.flatMap { evento ->
            val animal = evento.animal
            val tarefas = mutableListOf<AgendaOperacionalItemDto>()

            val existeDiagnosticoPosterior = diagnosticos.any { it.animal.id == animal.id && !it.data.isBefore(evento.data) }
            if (!existeDiagnosticoPosterior) {
                val dataPrevista = evento.data.plusDays(30)
                val dias = ChronoUnit.DAYS.between(hoje, dataPrevista)
                if (dias <= 21) {
                    tarefas += AgendaOperacionalItemDto(
                            id = "repro-diagnostico-${evento.id}",
                            categoria = CategoriaAgenda.REPRODUCAO,
                            origem = OrigemAgenda.HEURISTICA,
                            titulo = "Realizar diagnostico de gestacao",
                            descricao = "${evento.tipo.name.replace("_", " ")} registrada em ${evento.data}.",
                            dataPrevista = dataPrevista,
                            situacao = situacaoPara(dias),
                            prioridade = if (dias <= 0) PrioridadeAgenda.ALTA else PrioridadeAgenda.MEDIA,
                            diasParaVencimento = dias,
                            animal = AnimalResumoDto(animal.id!!, animal.brinco, animal.nome),
                            lote = animal.lote?.let { LoteResumoDto(it.id!!, it.nome) }
                    )
                }
            }

            val existePartoPosterior = partos.any { it.animal.id == animal.id && !it.data.isBefore(evento.data) }
            if (!existePartoPosterior) {
                val dataPrevista = evento.data.plusDays(285)
                val dias = ChronoUnit.DAYS.between(hoje, dataPrevista)
                if (dias in -7..30) {
                    tarefas += AgendaOperacionalItemDto(
                            id = "repro-parto-${evento.id}",
                            categoria = CategoriaAgenda.REPRODUCAO,
                            origem = OrigemAgenda.HEURISTICA,
                            titulo = "Monitorar janela de parto",
                            descricao = "Acompanhar matriz proxima da janela estimada de parto.",
                            dataPrevista = dataPrevista,
                            situacao = situacaoPara(dias),
                            prioridade = if (dias <= 3) PrioridadeAgenda.ALTA else PrioridadeAgenda.MEDIA,
                            diasParaVencimento = dias,
                            animal = AnimalResumoDto(animal.id!!, animal.brinco, animal.nome),
                            lote = animal.lote?.let { LoteResumoDto(it.id!!, it.nome) }
                    )
                }
            }

            tarefas
        }
    }

    private fun situacaoPara(dias: Long): SituacaoAgenda = when {
        dias < 0 -> SituacaoAgenda.ATRASADO
        dias == 0L -> SituacaoAgenda.HOJE
        else -> SituacaoAgenda.PROXIMO
    }

    private fun prioridadeRank(prioridade: PrioridadeAgenda): Int = when (prioridade) {
        PrioridadeAgenda.ALTA -> 0
        PrioridadeAgenda.MEDIA -> 1
        PrioridadeAgenda.BAIXA -> 2
    }
}
