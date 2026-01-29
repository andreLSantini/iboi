package com.iboi.rebanho.api.dto

import com.iboi.rebanho.domain.TipoEvento
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

data class RegistrarEventoRequest(
        val animalId: UUID,
        val tipo: TipoEvento,
        val data: LocalDate,
        val descricao: String,

        // Campos opcionais específicos
        val peso: BigDecimal? = null,
        val produto: String? = null,
        val dose: BigDecimal? = null,
        val unidadeMedida: String? = null,
        val loteDestinoId: UUID? = null,
        val valor: BigDecimal? = null
)

data class EventoDto(
        val id: UUID,
        val animal: AnimalResumoDto,
        val tipo: TipoEvento,
        val data: LocalDate,
        val descricao: String,
        val peso: BigDecimal?,
        val produto: String?,
        val dose: BigDecimal?,
        val unidadeMedida: String?,
        val loteDestino: LoteResumoDto?,
        val valor: BigDecimal?,
        val responsavel: String?
)

data class FiltrarEventosRequest(
        val tipo: TipoEvento? = null,
        val dataInicio: LocalDate? = null,
        val dataFim: LocalDate? = null,
        val animalId: UUID? = null
)
