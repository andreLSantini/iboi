package com.iboi.rebanho.api.dto

import com.iboi.rebanho.domain.CategoriaAnimal
import com.iboi.rebanho.domain.Raca
import com.iboi.rebanho.domain.Sexo
import com.iboi.rebanho.domain.StatusAnimal
import jakarta.validation.constraints.*
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

data class CadastrarAnimalRequest(
        @field:NotBlank(message = "Brinco é obrigatório")
        @field:Size(max = 50, message = "Brinco deve ter no máximo 50 caracteres")
        val brinco: String,

        @field:Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
        val nome: String? = null,

        @field:NotNull(message = "Sexo é obrigatório")
        val sexo: Sexo,

        @field:NotNull(message = "Raça é obrigatória")
        val raca: Raca,

        @field:NotNull(message = "Data de nascimento é obrigatória")
        @field:Past(message = "Data de nascimento deve estar no passado")
        val dataNascimento: LocalDate,

        @field:DecimalMin(value = "0.0", message = "Peso deve ser maior que zero")
        @field:DecimalMax(value = "9999.99", message = "Peso deve ser menor que 10000 kg")
        val pesoAtual: BigDecimal? = null,

        @field:NotNull(message = "Categoria é obrigatória")
        val categoria: CategoriaAnimal,

        val loteId: UUID? = null,
        val paiId: UUID? = null,
        val maeId: UUID? = null,

        @field:Size(max = 1000, message = "Observações devem ter no máximo 1000 caracteres")
        val observacoes: String? = null
)

data class AtualizarAnimalRequest(
        @field:Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
        val nome: String? = null,

        val raca: Raca? = null,

        @field:DecimalMin(value = "0.0", message = "Peso deve ser maior que zero")
        @field:DecimalMax(value = "9999.99", message = "Peso deve ser menor que 10000 kg")
        val pesoAtual: BigDecimal? = null,

        val categoria: CategoriaAnimal? = null,
        val loteId: UUID? = null,
        val status: StatusAnimal? = null,

        @field:Size(max = 1000, message = "Observações devem ter no máximo 1000 caracteres")
        val observacoes: String? = null
)

data class AnimalDto(
        val id: UUID,
        val brinco: String,
        val nome: String?,
        val sexo: Sexo,
        val raca: Raca,
        val dataNascimento: LocalDate,
        val idade: Int, // em meses
        val pesoAtual: BigDecimal?,
        val status: StatusAnimal,
        val categoria: CategoriaAnimal,
        val lote: LoteResumoDto?,
        val pai: AnimalResumoDto?,
        val mae: AnimalResumoDto?,
        val observacoes: String?
)

data class AnimalResumoDto(
        val id: UUID,
        val brinco: String,
        val nome: String?
)

data class LoteResumoDto(
        val id: UUID,
        val nome: String
)

data class FiltrarAnimaisRequest(
        val status: StatusAnimal? = null,
        val categoria: CategoriaAnimal? = null,
        val loteId: UUID? = null,
        val sexo: Sexo? = null
)
