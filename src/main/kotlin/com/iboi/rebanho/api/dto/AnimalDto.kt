package com.iboi.rebanho.api.dto

import com.iboi.rebanho.domain.CategoriaAnimal
import com.iboi.rebanho.domain.Raca
import com.iboi.rebanho.domain.Sexo
import com.iboi.rebanho.domain.StatusAnimal
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

data class CadastrarAnimalRequest(
        val brinco: String,
        val nome: String? = null,
        val sexo: Sexo,
        val raca: Raca,
        val dataNascimento: LocalDate,
        val pesoAtual: BigDecimal? = null,
        val categoria: CategoriaAnimal,
        val loteId: UUID? = null,
        val paiId: UUID? = null,
        val maeId: UUID? = null,
        val observacoes: String? = null
)

data class AtualizarAnimalRequest(
        val nome: String? = null,
        val raca: Raca? = null,
        val pesoAtual: BigDecimal? = null,
        val categoria: CategoriaAnimal? = null,
        val loteId: UUID? = null,
        val status: StatusAnimal? = null,
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
