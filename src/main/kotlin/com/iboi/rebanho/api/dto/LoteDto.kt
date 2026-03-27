package com.iboi.rebanho.api.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDateTime
import java.util.*

data class CadastrarLoteRequest(
        @field:NotBlank(message = "Nome do lote é obrigatório")
        @field:Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
        val nome: String,

        @field:Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
        val descricao: String? = null
)

data class AtualizarLoteRequest(
        @field:Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
        val nome: String? = null,

        @field:Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
        val descricao: String? = null,

        val ativo: Boolean? = null
)

data class LoteDto(
        val id: UUID,
        val nome: String,
        val descricao: String?,
        val ativo: Boolean,
        val quantidadeAnimais: Int,
        val criadoEm: LocalDateTime
)
