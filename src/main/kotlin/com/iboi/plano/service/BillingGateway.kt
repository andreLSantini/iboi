package com.iboi.plano.service

import com.iboi.identity.domain.Empresa
import com.iboi.plano.model.MetodoPagamento
import java.math.BigDecimal
import java.time.LocalDate

interface BillingGateway {
    fun createCharge(
            empresa: Empresa,
            valor: BigDecimal,
            metodoPagamento: MetodoPagamento,
            dueDate: LocalDate,
            description: String
    ): BillingChargeResult
}

data class BillingChargeResult(
        val success: Boolean,
        val transactionId: String,
        val provider: String,
        val invoiceUrl: String? = null,
        val bankSlipUrl: String? = null,
        val pixPayload: String? = null,
        val pixEncodedImage: String? = null
)
