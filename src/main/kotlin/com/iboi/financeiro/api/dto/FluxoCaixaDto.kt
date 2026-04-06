package com.iboi.financeiro.api.dto

import com.iboi.financeiro.domain.StatusLancamentoFinanceiro
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

enum class TipoMovimentoCaixa {
    ENTRADA,
    SAIDA
}

data class FluxoCaixaResumoDto(
        val dataInicio: LocalDate,
        val dataFim: LocalDate,
        val totalRecebido: BigDecimal,
        val totalPago: BigDecimal,
        val totalPrevistoReceber: BigDecimal,
        val totalPrevistoPagar: BigDecimal,
        val totalPendenteReceber: BigDecimal,
        val totalPendentePagar: BigDecimal,
        val saldoRealizado: BigDecimal,
        val saldoProjetado: BigDecimal,
        val totalVencido: BigDecimal
)

data class MovimentoFluxoCaixaDto(
        val id: UUID,
        val tipo: TipoMovimentoCaixa,
        val descricao: String,
        val origem: String,
        val valor: BigDecimal,
        val dataCompetencia: LocalDate,
        val dataVencimento: LocalDate,
        val dataLiquidacao: LocalDate?,
        val status: StatusLancamentoFinanceiro,
        val formaPagamento: String,
        val contraparte: String? = null
)

data class FluxoCaixaDto(
        val resumo: FluxoCaixaResumoDto,
        val movimentos: List<MovimentoFluxoCaixaDto>
)
