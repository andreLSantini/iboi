package com.iboi.identity.infrastructure.repository

import com.iboi.identity.domain.FarmModule
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface FarmModuleRepository : JpaRepository<FarmModule, UUID> {
    fun existsByFarmIdAndModuleCodeAndActiveTrue(farmId: UUID, moduleCode: String): Boolean
}