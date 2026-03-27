package com.iboi.identity.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "pastures")
class Pasture(
        @Id
        @GeneratedValue
        val id: UUID? = null,

        @Column(nullable = false)
        var name: String,

        @Column
        var areaHa: Double? = null,

        @Column
        var latitude: Double? = null,

        @Column
        var longitude: Double? = null,

        @Column(length = 4000)
        var geoJson: String? = null,

        @Column(length = 1000)
        var notes: String? = null,

        @Column(nullable = false)
        var active: Boolean = true,

        @Column(nullable = false)
        val createdAt: LocalDateTime = LocalDateTime.now(),

        @Column(nullable = false)
        var updatedAt: LocalDateTime = LocalDateTime.now(),

        @ManyToOne
        @JoinColumn(name = "farm_id", nullable = false)
        val farm: Farm
)
