package com.iboi.identity.infrastructure.repository

import com.iboi.identity.domain.Farm
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface FarmRepository : JpaRepository<Farm, UUID> {

    fun findByEmpresa_Id(companyId: UUID): List<Farm>

    fun findByIdAndEmpresa_Id(
            farmId: UUID,
            companyId: UUID
    ): Farm?
}