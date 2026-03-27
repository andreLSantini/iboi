package com.iboi.identity.domain

import jakarta.persistence.*
import java.util.*

import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(
        name = "user_farm_profiles",
        uniqueConstraints = [
                UniqueConstraint(columnNames = ["usuario_id", "farm_id"])
        ]
)
class UserFarmProfile(

        @Id
        @GeneratedValue
        val id: UUID? = null,

        @ManyToOne
        @JoinColumn(name = "usuario_id", nullable = false)
        val usuario: Usuario,

        @ManyToOne
        @JoinColumn(name = "farm_id", nullable = false)
        val farm: Farm,

        @Enumerated(EnumType.STRING)
        val role: FarmRole,

        val isDefault: Boolean = false
)

