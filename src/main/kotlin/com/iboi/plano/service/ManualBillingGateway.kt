package com.iboi.plano.service

import com.iboi.identity.domain.Empresa
import com.iboi.plano.model.MetodoPagamento
import com.iboi.plano.model.PeriodoPagamento
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.LocalDate

@Component
@ConditionalOnProperty(name = ["asaas.enabled"], havingValue = "false", matchIfMissing = true)
class ManualBillingGateway : BillingGateway {
    override fun createCharge(
            empresa: Empresa,
            valor: BigDecimal,
            metodoPagamento: MetodoPagamento,
            periodoPagamento: PeriodoPagamento,
            dueDate: LocalDate,
            description: String
    ): BillingChargeResult {
        return BillingChargeResult(
                success = true,
                transactionId = "manual-${empresa.id}-${System.currentTimeMillis()}",
                provider = "manual"
        )
    }
}
