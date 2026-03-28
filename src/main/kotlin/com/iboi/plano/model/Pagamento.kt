package com.iboi.plano.model

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "pagamentos")
class Pagamento(
        @Id
        @GeneratedValue
        val id: UUID? = null,

        @ManyToOne
        @JoinColumn(name = "assinatura_id", nullable = false)
        val assinatura: Assinatura,

        @Column(nullable = false, precision = 10, scale = 2)
        val valor: BigDecimal,

        @Column(nullable = false)
        val dataVencimento: LocalDateTime,

        @Column(nullable = true)
        var dataPagamento: LocalDateTime? = null,

        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        var status: StatusPagamento = StatusPagamento.PENDENTE,

        @Enumerated(EnumType.STRING)
        @Column(nullable = true)
        var metodoPagamento: MetodoPagamento? = null,

        @Column(nullable = true)
        var transacaoId: String? = null,

        @Column(nullable = true)
        var gatewayProvider: String? = null,

        @Column(name = "asaas_subscription_id", nullable = true, length = 255)
        var asaasSubscriptionId: String? = null,

        @Column(nullable = true, length = 500)
        var invoiceUrl: String? = null,

        @Column(nullable = true, length = 500)
        var bankSlipUrl: String? = null,

        @Column(nullable = true, length = 4000)
        var pixPayload: String? = null,

        @Lob
        @Column(nullable = true)
        var pixEncodedImage: String? = null,

        @Column(nullable = false)
        val criadoEm: LocalDateTime = LocalDateTime.now()
)

enum class StatusPagamento {
    PENDENTE,
    PAGO,
    VENCIDO,
    CANCELADO,
    REEMBOLSADO
}

enum class MetodoPagamento {
    CARTAO_CREDITO,
    BOLETO,
    PIX,
    TRANSFERENCIA
}
