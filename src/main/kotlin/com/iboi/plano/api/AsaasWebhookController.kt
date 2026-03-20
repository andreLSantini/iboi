package com.iboi.plano.api

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.iboi.plano.model.StatusAssinatura
import com.iboi.plano.model.StatusPagamento
import com.iboi.plano.repository.PagamentoRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@RestController
@RequestMapping("/webhooks/asaas")
class AsaasWebhookController(
        private val pagamentoRepository: PagamentoRepository,
        @Value("\${asaas.webhook-token:}") private val webhookToken: String
) {

    @PostMapping
    @Transactional
    fun receive(
            @RequestHeader(name = "asaas-access-token", required = false) token: String?,
            @RequestBody payload: AsaasWebhookPayload
    ): ResponseEntity<Map<String, String>> {
        if (webhookToken.isNotBlank() && token != webhookToken) {
            return ResponseEntity.status(401).body(mapOf("message" to "invalid webhook token"))
        }

        val paymentId = payload.payment?.id ?: return ResponseEntity.ok(mapOf("message" to "ignored"))
        val pagamento = pagamentoRepository.findByTransacaoId(paymentId)
                ?: return ResponseEntity.ok(mapOf("message" to "payment not mapped"))

        when (payload.event) {
            "PAYMENT_RECEIVED", "PAYMENT_CONFIRMED" -> {
                pagamento.status = StatusPagamento.PAGO
                pagamento.dataPagamento = LocalDateTime.now()
                pagamento.assinatura.status = StatusAssinatura.ATIVA
                pagamento.assinatura.dataVencimento =
                        pagamento.assinatura.proximaCobranca ?: pagamento.assinatura.dataVencimento
            }

            "PAYMENT_OVERDUE" -> pagamento.status = StatusPagamento.VENCIDO
            "PAYMENT_DELETED", "PAYMENT_CANCELED" -> pagamento.status = StatusPagamento.CANCELADO
            "PAYMENT_REFUNDED" -> pagamento.status = StatusPagamento.REEMBOLSADO
        }

        pagamentoRepository.save(pagamento)
        return ResponseEntity.ok(mapOf("message" to "processed"))
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class AsaasWebhookPayload(
        val event: String,
        val payment: AsaasWebhookPayment? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class AsaasWebhookPayment(
        val id: String? = null
)
