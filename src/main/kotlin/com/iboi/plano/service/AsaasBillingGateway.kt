package com.iboi.plano.service

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.iboi.identity.domain.Empresa
import com.iboi.identity.infrastructure.repository.EmpresaRepository
import com.iboi.plano.model.MetodoPagamento
import com.iboi.plano.model.PeriodoPagamento
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.RestClient
import java.math.BigDecimal
import java.time.LocalDate

@Component
@ConditionalOnProperty(name = ["asaas.enabled"], havingValue = "true")
class AsaasBillingGateway(
        @Value("\${asaas.base-url}") private val baseUrl: String,
        @Value("\${asaas.api-key}") private val apiKey: String,
        @Value("\${asaas.user-agent:iboi-billing}") private val userAgent: String,
        private val empresaRepository: EmpresaRepository
) : BillingGateway {

    private val restClient = RestClient.builder()
            .baseUrl(baseUrl)
            .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader("access_token", apiKey)
            .defaultHeader("User-Agent", userAgent)
            .build()

    override fun createCharge(
            empresa: Empresa,
            valor: BigDecimal,
            metodoPagamento: MetodoPagamento,
            periodoPagamento: PeriodoPagamento,
            dueDate: LocalDate,
            description: String
    ): BillingChargeResult {
        if (apiKey.isBlank()) {
            throw IllegalStateException("ASAAS_API_KEY nao configurada no ambiente")
        }

        val customerId = ensureCustomer(empresa)
        val billingType = when (metodoPagamento) {
            MetodoPagamento.PIX -> "PIX"
            MetodoPagamento.BOLETO -> "BOLETO"
            MetodoPagamento.CARTAO_CREDITO -> throw IllegalArgumentException("Cartao de credito exige checkout/tokenizacao especifica no Asaas")
            MetodoPagamento.TRANSFERENCIA -> throw IllegalArgumentException("Transferencia nao e suportada para cobrancas Asaas")
        }
        val cycle = when (periodoPagamento) {
            PeriodoPagamento.MENSAL -> "MONTHLY"
            PeriodoPagamento.SEMESTRAL -> "SEMIANNUALLY"
            PeriodoPagamento.ANUAL -> "YEARLY"
        }

        try {
            val subscription = restClient.post()
                    .uri("/v3/subscriptions")
                    .body(
                            mapOf(
                                    "customer" to customerId,
                                    "billingType" to billingType,
                                    "cycle" to cycle,
                                    "value" to valor,
                                    "nextDueDate" to dueDate.toString(),
                                    "description" to description
                            )
                    )
                    .retrieve()
                    .body(AsaasSubscriptionResponse::class.java)
                    ?: throw IllegalStateException("Asaas nao retornou a assinatura recorrente criada")

            val payment = restClient.get()
                    .uri("/v3/subscriptions/${subscription.id}/payments")
                    .retrieve()
                    .body(AsaasPaymentListResponse::class.java)
                    ?.data
                    ?.firstOrNull()

            val pixInfo = if (billingType == "PIX" && payment?.id != null) {
                restClient.get()
                        .uri("/v3/payments/${payment.id}/pixQrCode")
                        .retrieve()
                        .body(AsaasPixQrCodeResponse::class.java)
            } else {
                null
            }

            return BillingChargeResult(
                    success = true,
                    transactionId = payment?.id ?: subscription.id,
                    provider = "asaas",
                    recurringSubscriptionId = subscription.id,
                    invoiceUrl = payment?.invoiceUrl,
                    bankSlipUrl = payment?.bankSlipUrl,
                    pixPayload = pixInfo?.payload,
                    pixEncodedImage = pixInfo?.encodedImage
            )
        } catch (ex: RestClientResponseException) {
            val body = ex.responseBodyAsString
                    ?.takeIf { it.isNotBlank() }
                    ?.replace('\n', ' ')
                    ?.take(400)
            val suffix = body?.let { ": $it" } ?: ""
            throw IllegalStateException("Falha ao criar cobranca no Asaas (HTTP ${ex.statusCode.value()})$suffix")
        } catch (ex: Exception) {
            if (ex is IllegalArgumentException || ex is IllegalStateException) {
                throw ex
            }
            throw IllegalStateException("Falha ao criar cobranca no Asaas: ${ex.message}", ex)
        }
    }

    private fun ensureCustomer(empresa: Empresa): String {
        empresa.asaasCustomerId?.let { return it }

        val customer = try {
            restClient.post()
                    .uri("/v3/customers")
                    .body(
                            mapOf(
                                    "name" to empresa.nome,
                                    "cpfCnpj" to empresa.cnpj
                            ).filterValues { it != null }
                    )
                    .retrieve()
                    .body(AsaasCustomerResponse::class.java)
                    ?: throw IllegalStateException("Asaas nao retornou o cliente criado")
        } catch (ex: RestClientResponseException) {
            val body = ex.responseBodyAsString
                    ?.takeIf { it.isNotBlank() }
                    ?.replace('\n', ' ')
                    ?.take(400)
            val suffix = body?.let { ": $it" } ?: ""
            throw IllegalStateException("Falha ao criar cliente no Asaas (HTTP ${ex.statusCode.value()})$suffix")
        }

        empresa.asaasCustomerId = customer.id
        empresaRepository.save(empresa)
        return customer.id
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class AsaasCustomerResponse(
            val id: String
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class AsaasPaymentResponse(
            val id: String,
            val invoiceUrl: String? = null,
            val bankSlipUrl: String? = null,
            val subscription: String? = null,
            val value: BigDecimal? = null,
            val dueDate: String? = null,
            val billingType: String? = null
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class AsaasPaymentListResponse(
            val data: List<AsaasPaymentResponse> = emptyList()
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class AsaasSubscriptionResponse(
            val id: String
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class AsaasPixQrCodeResponse(
            val encodedImage: String? = null,
            val payload: String? = null
    )
}
