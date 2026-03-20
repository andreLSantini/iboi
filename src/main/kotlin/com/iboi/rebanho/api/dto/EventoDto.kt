package com.iboi.rebanho.api.dto

import com.iboi.rebanho.domain.TipoEvento
import jakarta.validation.constraints.*
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

data class RegistrarEventoRequest(
        @field:NotNull(message = "Animal é obrigatório")
        val animalId: UUID,

        @field:NotNull(message = "Tipo de evento é obrigatório")
        val tipo: TipoEvento,

        @field:NotNull(message = "Data é obrigatória")
        @field:PastOrPresent(message = "Data não pode ser futura")
        val data: LocalDate,

        @field:NotBlank(message = "Descrição é obrigatória")
        @field:Size(max = 1000, message = "Descrição deve ter no máximo 1000 caracteres")
        val descricao: String,

        // Campos opcionais específicos
        @field:DecimalMin(value = "0.0", message = "Peso deve ser maior que zero")
        val peso: BigDecimal? = null,

        @field:Size(max = 200, message = "Produto deve ter no máximo 200 caracteres")
        val produto: String? = null,

        @field:DecimalMin(value = "0.0", message = "Dose deve ser maior que zero")
        val dose: BigDecimal? = null,

        @field:Size(max = 20, message = "Unidade de medida deve ter no máximo 20 caracteres")
        val unidadeMedida: String? = null,

        val loteDestinoId: UUID? = null,

        @field:DecimalMin(value = "0.0", message = "Valor deve ser maior que zero")
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
