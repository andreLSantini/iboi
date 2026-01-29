package com.iboi.plano.api.dto

import com.iboi.plano.model.PeriodoPagamento
import com.iboi.plano.model.StatusAssinatura
import com.iboi.plano.model.TipoAssinatura
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

data class AssinaturaDto(
        val id: UUID,
        val tipo: TipoAssinatura,
        val status: StatusAssinatura,
        val periodoPagamento: PeriodoPagamento?,
        val dataInicio: LocalDateTime,
        val dataVencimento: LocalDateTime,
        val proximaCobranca: LocalDateTime?,
        val valor: BigDecimal?,
        val diasRestantes: Long
)

data class UpgradeRequest(
        val novoPlano: TipoAssinatura,
        val periodo: PeriodoPagamento
)

data class CancelarAssinaturaRequest(
        val motivo: String? = null
)
