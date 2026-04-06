package com.iboi.rebanho.api.dto

import com.iboi.rebanho.domain.CategoriaAnimal
import com.iboi.rebanho.domain.OrigemAnimal
import com.iboi.rebanho.domain.Raca
import com.iboi.rebanho.domain.Sexo
import com.iboi.rebanho.domain.StatusAnimal
import com.iboi.rebanho.domain.TipoMovimentacaoAnimal
import com.iboi.rebanho.domain.TipoVacina
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Past
import jakarta.validation.constraints.Size
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.util.UUID

data class CadastrarAnimalRequest(
        @field:NotBlank(message = "Brinco e obrigatorio")
        @field:Size(max = 50, message = "Brinco deve ter no maximo 50 caracteres")
        val brinco: String,

        @field:Size(max = 64, message = "RFID deve ter no maximo 64 caracteres")
        val rfid: String? = null,

        @field:Size(max = 64, message = "Codigo SISBOV deve ter no maximo 64 caracteres")
        val codigoSisbov: String? = null,

        @field:Size(max = 100, message = "Nome deve ter no maximo 100 caracteres")
        val nome: String? = null,

        @field:NotNull(message = "Sexo e obrigatorio")
        val sexo: Sexo,

        @field:NotNull(message = "Raca e obrigatoria")
        val raca: Raca,

        @field:NotNull(message = "Data de nascimento e obrigatoria")
        @field:Past(message = "Data de nascimento deve estar no passado")
        val dataNascimento: LocalDate,

        @field:DecimalMin(value = "0.0", message = "Peso deve ser maior que zero")
        val pesoAtual: BigDecimal? = null,

        @field:NotNull(message = "Categoria e obrigatoria")
        val categoria: CategoriaAnimal,

        @field:NotNull(message = "Origem e obrigatoria")
        val origem: OrigemAnimal = OrigemAnimal.NASCIMENTO,

        val loteId: UUID? = null,
        val pastureId: UUID? = null,
        val paiId: UUID? = null,
        val maeId: UUID? = null,
        val dataEntrada: LocalDate? = null,
        val sisbovAtivo: Boolean = false,

        @field:Size(max = 1000, message = "Observacoes devem ter no maximo 1000 caracteres")
        val observacoes: String? = null
)

data class AtualizarAnimalRequest(
        @field:Size(max = 64, message = "RFID deve ter no maximo 64 caracteres")
        val rfid: String? = null,

        @field:Size(max = 64, message = "Codigo SISBOV deve ter no maximo 64 caracteres")
        val codigoSisbov: String? = null,

        @field:Size(max = 100, message = "Nome deve ter no maximo 100 caracteres")
        val nome: String? = null,

        val raca: Raca? = null,

        @field:DecimalMin(value = "0.0", message = "Peso deve ser maior que zero")
        val pesoAtual: BigDecimal? = null,

        val categoria: CategoriaAnimal? = null,
        val origem: OrigemAnimal? = null,
        val loteId: UUID? = null,
        val pastureId: UUID? = null,
        val status: StatusAnimal? = null,
        val dataEntrada: LocalDate? = null,
        val sisbovAtivo: Boolean? = null,

        @field:Size(max = 1000, message = "Observacoes devem ter no maximo 1000 caracteres")
        val observacoes: String? = null
)

data class AnimalDto(
        val id: UUID,
        val brinco: String,
        val rfid: String?,
        val codigoSisbov: String?,
        val sisbovAtivo: Boolean,
        val nome: String?,
        val sexo: Sexo,
        val raca: Raca,
        val dataNascimento: LocalDate,
        val dataEntrada: LocalDate?,
        val idade: Int,
        val pesoAtual: BigDecimal?,
        val status: StatusAnimal,
        val categoria: CategoriaAnimal,
        val origem: OrigemAnimal,
        val lote: LoteResumoDto?,
        val pasture: PastureResumoDto?,
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

data class PastureResumoDto(
        val id: UUID,
        val nome: String
)

data class FarmResumoDto(
        val id: UUID,
        val nome: String
)

data class FiltrarAnimaisRequest(
        val status: StatusAnimal? = null,
        val categoria: CategoriaAnimal? = null,
        val loteId: UUID? = null,
        val sexo: Sexo? = null
)

data class RegistrarMovimentacaoAnimalRequest(
        @field:NotNull(message = "Tipo de movimentacao e obrigatorio")
        val tipo: TipoMovimentacaoAnimal,

        @field:NotNull(message = "Data da movimentacao e obrigatoria")
        val movimentadaEm: LocalDate,

        val destinoFarmId: UUID? = null,
        val destinoPastureId: UUID? = null,

        @field:Size(max = 64, message = "Numero GTA deve ter no maximo 64 caracteres")
        val numeroGta: String? = null,

        @field:Size(max = 128, message = "Documento externo deve ter no maximo 128 caracteres")
        val documentoExterno: String? = null,

        @field:Size(max = 255, message = "Motivo deve ter no maximo 255 caracteres")
        val motivo: String? = null,

        @field:Size(max = 1000, message = "Observacoes devem ter no maximo 1000 caracteres")
        val observacoes: String? = null
)

data class MovimentacaoAnimalDto(
        val id: UUID,
        val tipo: TipoMovimentacaoAnimal,
        val movimentadaEm: LocalDate,
        val farmOrigem: FarmResumoDto?,
        val farmDestino: FarmResumoDto?,
        val pastureOrigem: PastureResumoDto?,
        val pastureDestino: PastureResumoDto?,
        val numeroGta: String?,
        val documentoExterno: String?,
        val motivo: String?,
        val observacoes: String?,
        val responsavel: String?
)

data class RegistrarVacinacaoAnimalRequest(
        @field:NotNull(message = "Tipo de vacina e obrigatorio")
        val tipo: TipoVacina,

        @field:NotBlank(message = "Nome da vacina e obrigatorio")
        @field:Size(max = 120, message = "Nome da vacina deve ter no maximo 120 caracteres")
        val nomeVacina: String,

        @field:DecimalMin(value = "0.0", message = "Dose deve ser positiva")
        val dose: BigDecimal? = null,

        @field:Size(max = 30, message = "Unidade deve ter no maximo 30 caracteres")
        val unidadeMedida: String? = null,

        @field:NotNull(message = "Data da aplicacao e obrigatoria")
        val aplicadaEm: LocalDate,

        val proximaDoseEm: LocalDate? = null,

        @field:Size(max = 120, message = "Fabricante deve ter no maximo 120 caracteres")
        val fabricante: String? = null,

        @field:Size(max = 80, message = "Lote da vacina deve ter no maximo 80 caracteres")
        val loteVacina: String? = null,

        @field:Size(max = 1000, message = "Observacoes devem ter no maximo 1000 caracteres")
        val observacoes: String? = null
)

data class VacinacaoAnimalDto(
        val id: UUID,
        val tipo: TipoVacina,
        val nomeVacina: String,
        val dose: BigDecimal?,
        val unidadeMedida: String?,
        val aplicadaEm: LocalDate,
        val proximaDoseEm: LocalDate?,
        val fabricante: String?,
        val loteVacina: String?,
        val observacoes: String?,
        val responsavel: String?
)

data class AnimalFichaCompletaDto(
        val animal: AnimalDto,
        val pesagens: List<PesagemAnimalDto>,
        val eventos: List<EventoDto>,
        val eventosReprodutivos: List<EventoDto>,
        val vacinacoes: List<VacinacaoAnimalDto>,
        val movimentacoes: List<MovimentacaoAnimalDto>
)

data class PesagemAnimalDto(
        val id: UUID,
        val data: LocalDate,
        val peso: BigDecimal,
        val unidade: String = "kg",
        val variacaoPeso: BigDecimal? = null,
        val diasDesdeAnterior: Long? = null,
        val ganhoMedioDiario: BigDecimal? = null,
        val observacao: String? = null,
        val responsavel: String? = null
)

fun calcularGanhoMedioDiario(variacaoPeso: BigDecimal, dias: Long): BigDecimal? {
    if (dias <= 0) return null
    return variacaoPeso.divide(BigDecimal.valueOf(dias), 3, RoundingMode.HALF_UP)
}
