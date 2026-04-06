package com.iboi.financeiro.api.dto

import com.iboi.financeiro.domain.FormaPagamento
import com.iboi.financeiro.domain.StatusLancamentoFinanceiro
import com.iboi.financeiro.domain.TipoReceita
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

data class RegistrarReceitaRequest(
        val tipo: TipoReceita,
        val descricao: String,
        val valor: BigDecimal,
        val data: LocalDate,
        val dataVencimento: LocalDate? = null,
        val dataLiquidacao: LocalDate? = null,
        val status: StatusLancamentoFinanceiro? = null,
        val formaPagamento: FormaPagamento,
        val loteId: UUID? = null,
        val animalId: UUID? = null,
        val comprador: String? = null,
        val quantidadeAnimais: Int? = null,
        val observacoes: String? = null
)

data class ReceitaDto(
        val id: UUID,
        val tipo: TipoReceita,
        val descricao: String,
        val valor: BigDecimal,
        val data: LocalDate,
        val dataVencimento: LocalDate,
        val dataLiquidacao: LocalDate?,
        val status: StatusLancamentoFinanceiro,
        val formaPagamento: FormaPagamento,
        val comprador: String?,
        val quantidadeAnimais: Int?,
        val responsavel: String?,
        val observacoes: String?
)

data class ResumoReceitasPorTipo(
        val tipo: TipoReceita,
        val total: BigDecimal
)
