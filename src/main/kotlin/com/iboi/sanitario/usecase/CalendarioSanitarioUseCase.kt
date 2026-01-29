package com.iboi.sanitario.usecase

import com.iboi.rebanho.api.dto.AnimalResumoDto
import com.iboi.sanitario.api.dto.AgendamentoDto
import com.iboi.sanitario.api.dto.CalendarioSanitarioResponse
import com.iboi.sanitario.domain.StatusAgendamento
import com.iboi.sanitario.repository.AgendamentoSanitarioRepository
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.*

@Component
class CalendarioSanitarioUseCase(
        private val agendamentoRepository: AgendamentoSanitarioRepository
) {

    fun execute(farmId: UUID): CalendarioSanitarioResponse {
        val hoje = LocalDate.now()
        val daqui30Dias = hoje.plusDays(30)

        val pendentes = agendamentoRepository.findByFarmIdAndStatus(farmId, StatusAgendamento.PENDENTE)

        val atrasados = pendentes.filter { it.dataPrevista.isBefore(hoje) }
        val proximos = pendentes.filter {
            it.dataPrevista.isAfter(hoje) && it.dataPrevista.isBefore(daqui30Dias)
        }

        return CalendarioSanitarioResponse(
                pendentes = pendentes.map { toDto(it) },
                atrasados = atrasados.map { toDto(it) },
                proximos30Dias = proximos.map { toDto(it) }
        )
    }

    private fun toDto(agendamento: com.iboi.sanitario.domain.AgendamentoSanitario): AgendamentoDto {
        val hoje = LocalDate.now()
        val diasAte = ChronoUnit.DAYS.between(hoje, agendamento.dataPrevista)

        return AgendamentoDto(
                id = agendamento.id!!,
                animal = AnimalResumoDto(
                        id = agendamento.animal.id!!,
                        brinco = agendamento.animal.brinco,
                        nome = agendamento.animal.nome
                ),
                tipo = agendamento.itemProtocolo.tipo,
                produto = agendamento.itemProtocolo.produto,
                dataPrevista = agendamento.dataPrevista,
                status = agendamento.status,
                diasAteVencimento = diasAte
        )
    }
}
