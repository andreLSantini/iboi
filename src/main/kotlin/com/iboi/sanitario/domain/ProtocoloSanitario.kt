package com.iboi.sanitario.domain

import com.iboi.identity.domain.Farm
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "protocolos_sanitarios")
class ProtocoloSanitario(
        @Id
        @GeneratedValue
        val id: UUID? = null,

        @Column(nullable = false)
        var nome: String,

        @Column(length = 500)
        var descricao: String? = null,

        @ManyToOne
        @JoinColumn(name = "farm_id", nullable = false)
        val farm: Farm,

        @Column(nullable = false)
        var ativo: Boolean = true,

        @Column(nullable = false)
        val criadoEm: LocalDateTime = LocalDateTime.now()
)
