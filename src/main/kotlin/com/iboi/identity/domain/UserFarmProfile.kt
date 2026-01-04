package com.iboi.identity.domain

import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "user_farm_profiles")
class UserFarmProfile(
        @Id @GeneratedValue
        val id: UUID? = null,

        val farmId: UUID,

        @ManyToOne
        val user: User,

        @ManyToOne
        val profile: Profile
)
