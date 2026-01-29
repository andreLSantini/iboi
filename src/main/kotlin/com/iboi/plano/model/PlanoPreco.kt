package com.iboi.plano.model

import java.math.BigDecimal

object PlanoPreco {

    private val tabelaPrecos = mapOf(
            TipoAssinatura.TRIAL to mapOf(
                    PeriodoPagamento.MENSAL to BigDecimal.ZERO
            ),
            TipoAssinatura.BASIC to mapOf(
                    PeriodoPagamento.MENSAL to BigDecimal("99.00"),
                    PeriodoPagamento.SEMESTRAL to BigDecimal("534.00"),
                    PeriodoPagamento.ANUAL to BigDecimal("948.00")
            ),
            TipoAssinatura.PRO to mapOf(
                    PeriodoPagamento.MENSAL to BigDecimal("199.00"),
                    PeriodoPagamento.SEMESTRAL to BigDecimal("1074.00"),
                    PeriodoPagamento.ANUAL to BigDecimal("1908.00")
            ),
            TipoAssinatura.ENTERPRISE to mapOf(
                    PeriodoPagamento.MENSAL to BigDecimal("399.00"),
                    PeriodoPagamento.SEMESTRAL to BigDecimal("2154.00"),
                    PeriodoPagamento.ANUAL to BigDecimal("3828.00")
            )
    )

    fun getValor(tipo: TipoAssinatura, periodo: PeriodoPagamento): BigDecimal {
        return tabelaPrecos[tipo]?.get(periodo)
                ?: throw IllegalArgumentException("Preço não encontrado para $tipo - $periodo")
    }

    fun getDesconto(periodo: PeriodoPagamento): String {
        return when (periodo) {
            PeriodoPagamento.MENSAL -> "Sem desconto"
            PeriodoPagamento.SEMESTRAL -> "10% de desconto"
            PeriodoPagamento.ANUAL -> "20% de desconto"
        }
    }
}
