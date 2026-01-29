package com.iboi.identity.domain

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.*

@Entity
@Table(name = "farm_modules")
class FarmModule(
        @Id @GeneratedValue
        val id: UUID? = null,

        val farmId: UUID,
        val moduleCode: String,
        val active: Boolean = true
)
