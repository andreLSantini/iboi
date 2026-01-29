package com.iboi.sanitario.api.dto

import com.iboi.rebanho.api.dto.AnimalResumoDto
import com.iboi.sanitario.domain.StatusAgendamento
import com.iboi.sanitario.domain.TipoAplicacao
import java.time.LocalDate
import java.util.*

data class AgendamentoDto(
        val id: UUID,
        val animal: AnimalResumoDto,
        val tipo: TipoAplicacao,
        val produto: String,
        val dataPrevista: LocalDate,
        val status: StatusAgendamento,
        val diasAteVencimento: Long
)

data class CalendarioSanitarioResponse(
        val pendentes: List<AgendamentoDto>,
        val atrasados: List<AgendamentoDto>,
        val proximos30Dias: List<AgendamentoDto>
)
