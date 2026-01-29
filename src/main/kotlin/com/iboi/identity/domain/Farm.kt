package com.iboi.identity.domain

import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "farms")
class Farm(

        @Id
        @GeneratedValue
        val id: UUID? = null,

        @Column(nullable = false)
        val name: String,

        @Column(nullable = false)
        val city: String,

        @Column(nullable = false)
        val state: String,

        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        val productionType: ProductionType,

        @Column
        val size: Double? = null,

        @ManyToOne
        @JoinColumn(name = "empresa_id", nullable = false)
        val empresa: Empresa
)