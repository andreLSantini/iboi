package com.iboi.identity.domain

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "farms")
class Farm(

        @Id
        @GeneratedValue
        val id: UUID? = null,

        @Column(nullable = false)
        var name: String,

        @Column(nullable = false)
        var city: String,

        @Column(nullable = false)
        var state: String,

        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        var productionType: ProductionType,

        @Column
        var size: Double? = null,

        @Column
        var ownerName: String? = null,

        @Column
        var ownerDocument: String? = null,

        @Column
        var phone: String? = null,

        @Column
        var email: String? = null,

        @Column
        var addressLine: String? = null,

        @Column
        var zipCode: String? = null,

        @Column
        var latitude: Double? = null,

        @Column
        var longitude: Double? = null,

        @Column
        var legalStatus: String? = null,

        @Column
        var documentProof: String? = null,

        @Column
        var ccir: String? = null,

        @Column
        var cib: String? = null,

        @Column
        var car: String? = null,

        @Column
        var mainExploration: String? = null,

        @Column
        var estimatedCapacity: Int? = null,

        @Column
        var grazingArea: Double? = null,

        @Column
        var legalReserveArea: Double? = null,

        @Column
        var appArea: Double? = null,

        @Column
        var productiveArea: Double? = null,

        @Column(nullable = false)
        var active: Boolean = true,

        @Column(nullable = false)
        val createdAt: LocalDateTime = LocalDateTime.now(),

        @Column(nullable = false)
        var updatedAt: LocalDateTime = LocalDateTime.now(),

        @ManyToOne
        @JoinColumn(name = "empresa_id", nullable = false)
        val empresa: Empresa
)
