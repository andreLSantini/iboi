package com.iboi.plano.model

import com.iboi.tenant.Empresa
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "assinaturas")
class Assinatura(
        @Id
        val id: UUID = UUID.randomUUID(),

        @OneToOne
        val empresa: Empresa,

        @Enumerated(EnumType.STRING)
        val tipo: TipoAssinatura,

        val trialAte: LocalDateTime?,

        val ativa: Boolean = true
)
