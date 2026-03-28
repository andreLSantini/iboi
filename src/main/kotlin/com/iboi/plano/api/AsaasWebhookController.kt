package com.iboi.plano.api

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.iboi.plano.model.MetodoPagamento
import com.iboi.plano.model.Pagamento
import com.iboi.plano.model.StatusAssinatura
import com.iboi.plano.model.StatusPagamento
import com.iboi.plano.repository.AssinaturaRepository
import com.iboi.plano.repository.PagamentoRepository
import com.iboi.plano.service.AssinaturaService
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@RestController
@RequestMapping("/webhooks/asaas")
class AsaasWebhookController(
        private val pagamentoRepository: PagamentoRepository,
        private val assinaturaRepository: AssinaturaRepository,
        private val assinaturaService: AssinaturaService,
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
                ?: resolvePagamento(payload)
                ?: return ResponseEntity.ok(mapOf("message" to "payment not mapped"))

        when (payload.event) {
            "PAYMENT_CREATED" -> {
                pagamento.status = StatusPagamento.PENDENTE
                pagamento.invoiceUrl = payload.payment.invoiceUrl ?: pagamento.invoiceUrl
                pagamento.bankSlipUrl = payload.payment.bankSlipUrl ?: pagamento.bankSlipUrl
                pagamento.pixPayload = payload.payment.pixPayload ?: pagamento.pixPayload
                pagamento.pixEncodedImage = payload.payment.pixEncodedImage ?: pagamento.pixEncodedImage
            }

            "PAYMENT_RECEIVED", "PAYMENT_CONFIRMED" -> {
                pagamento.status = StatusPagamento.PAGO
                pagamento.dataPagamento = LocalDateTime.now()
                pagamento.assinatura.status = StatusAssinatura.ATIVA
                val dueDate = payload.payment.dueDate?.let { LocalDate.parse(it).atStartOfDay() }
                        ?: pagamento.dataVencimento
                val periodo = pagamento.assinatura.periodoPagamento
                if (periodo != null) {
                    pagamento.assinatura.dataVencimento =
                            assinaturaService.calcularProximaCobranca(dueDate, periodo)
                    pagamento.assinatura.proximaCobranca = pagamento.assinatura.dataVencimento
                }
            }

            "PAYMENT_OVERDUE" -> pagamento.status = StatusPagamento.VENCIDO
            "PAYMENT_DELETED", "PAYMENT_CANCELED" -> pagamento.status = StatusPagamento.CANCELADO
            "PAYMENT_REFUNDED" -> pagamento.status = StatusPagamento.REEMBOLSADO
        }

        pagamentoRepository.save(pagamento)
        return ResponseEntity.ok(mapOf("message" to "processed"))
    }

    private fun resolvePagamento(payload: AsaasWebhookPayload): Pagamento? {
        val payment = payload.payment ?: return null
        val subscriptionId = payment.subscription ?: return null
        val assinatura = assinaturaRepository.findByAsaasSubscriptionId(subscriptionId) ?: return null

        val existing = pagamentoRepository.findByAsaasSubscriptionIdOrderByDataVencimentoDesc(subscriptionId)
                .firstOrNull { it.transacaoId == payment.id }
        if (existing != null) {
            return existing
        }

        return pagamentoRepository.save(
                Pagamento(
                        assinatura = assinatura,
                        valor = payment.value ?: assinatura.valor ?: BigDecimal.ZERO,
                        dataVencimento = payment.dueDate?.let { LocalDate.parse(it).atStartOfDay() }
                                ?: assinatura.dataVencimento,
                        status = when (payload.event) {
                            "PAYMENT_RECEIVED", "PAYMENT_CONFIRMED" -> StatusPagamento.PAGO
                            "PAYMENT_OVERDUE" -> StatusPagamento.VENCIDO
                            "PAYMENT_DELETED", "PAYMENT_CANCELED" -> StatusPagamento.CANCELADO
                            "PAYMENT_REFUNDED" -> StatusPagamento.REEMBOLSADO
                            else -> StatusPagamento.PENDENTE
                        },
                        metodoPagamento = payment.billingType?.toMetodoPagamento(),
                        transacaoId = payment.id,
                        gatewayProvider = "asaas",
                        asaasSubscriptionId = subscriptionId,
                        invoiceUrl = payment.invoiceUrl,
                        bankSlipUrl = payment.bankSlipUrl,
                        pixPayload = payment.pixPayload,
                        pixEncodedImage = payment.pixEncodedImage
                )
        )
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class AsaasWebhookPayload(
        val event: String,
        val payment: AsaasWebhookPayment? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class AsaasWebhookPayment(
        val id: String? = null,
        val subscription: String? = null,
        val value: BigDecimal? = null,
        val dueDate: String? = null,
        val billingType: String? = null,
        val invoiceUrl: String? = null,
        val bankSlipUrl: String? = null,
        val pixPayload: String? = null,
        val pixEncodedImage: String? = null
)

private fun String.toMetodoPagamento(): MetodoPagamento? = when (this.uppercase()) {
    "PIX" -> MetodoPagamento.PIX
    "BOLETO" -> MetodoPagamento.BOLETO
    "CREDIT_CARD" -> MetodoPagamento.CARTAO_CREDITO
    else -> null
}
