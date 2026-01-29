package com.iboi.plano.model

import com.iboi.identity.domain.Empresa
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "assinaturas")
class Assinatura(
        @Id
        @GeneratedValue
        val id: UUID? = null,

        @OneToOne
        @JoinColumn(name = "empresa_id", nullable = false)
        val empresa: Empresa,

        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        var tipo: TipoAssinatura,

        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        var status: StatusAssinatura = StatusAssinatura.TRIAL,

        @Enumerated(EnumType.STRING)
        @Column(nullable = true)
        var periodoPagamento: PeriodoPagamento? = null,

        @Column(nullable = false)
        val dataInicio: LocalDateTime = LocalDateTime.now(),

        @Column(nullable = false)
        var dataVencimento: LocalDateTime,

        @Column(nullable = true)
        var proximaCobranca: LocalDateTime? = null,

        @Column(nullable = true, precision = 10, scale = 2)
        var valor: BigDecimal? = null,

        @Column(nullable = false)
        val criadaEm: LocalDateTime = LocalDateTime.now()
)
