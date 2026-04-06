package com.iboi.financeiro.api.dto

import com.iboi.financeiro.domain.CategoriaDespesa
import com.iboi.financeiro.domain.FormaPagamento
import com.iboi.financeiro.domain.StatusLancamentoFinanceiro
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

data class RegistrarDespesaRequest(
        val categoria: CategoriaDespesa,
        val descricao: String,
        val valor: BigDecimal,
        val data: LocalDate,
        val dataVencimento: LocalDate? = null,
        val dataLiquidacao: LocalDate? = null,
        val status: StatusLancamentoFinanceiro? = null,
        val formaPagamento: FormaPagamento,
        val loteId: UUID? = null,
        val animalId: UUID? = null,
        val observacoes: String? = null
)

data class DespesaDto(
        val id: UUID,
        val categoria: CategoriaDespesa,
        val descricao: String,
        val valor: BigDecimal,
        val data: LocalDate,
        val dataVencimento: LocalDate,
        val dataLiquidacao: LocalDate?,
        val status: StatusLancamentoFinanceiro,
        val formaPagamento: FormaPagamento,
        val responsavel: String?,
        val observacoes: String?
)

data class ResumoDespesasPorCategoria(
        val categoria: CategoriaDespesa,
        val total: BigDecimal
)
