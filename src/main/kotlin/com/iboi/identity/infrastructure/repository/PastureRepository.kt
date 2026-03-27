package com.iboi.identity.infrastructure.repository

import com.iboi.identity.domain.Pasture
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface PastureRepository : JpaRepository<Pasture, UUID> {
    fun findByFarmIdOrderByNameAsc(farmId: UUID): List<Pasture>
    fun findByFarmIdAndNameIgnoreCase(farmId: UUID, name: String): Pasture?
}
