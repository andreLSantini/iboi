package com.iboi.ia.api.dto

import com.iboi.ia.domain.PrioridadeAlerta
import com.iboi.ia.domain.StatusAlerta
import com.iboi.ia.domain.TipoAlerta
import com.iboi.rebanho.api.dto.AnimalResumoDto
import java.time.LocalDateTime
import java.util.*

data class AlertaDto(
        val id: UUID,
        val tipo: TipoAlerta,
        val prioridade: PrioridadeAlerta,
        val titulo: String,
        val mensagem: String,
        val animal: AnimalResumoDto?,
        val recomendacao: String?,
        val status: StatusAlerta,
        val criadoEm: LocalDateTime
)
