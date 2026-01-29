package com.iboi.identity.infrastructure.repository

import com.iboi.identity.domain.UserFarmProfile
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface UserFarmProfileRepository : JpaRepository<UserFarmProfile, UUID> {

    fun findByUsuario_Id(
            userId: UUID,
    ): UserFarmProfile?
    fun findAllByUsuario_Id(userId: UUID): List<UserFarmProfile>

    fun findByUsuario_IdAndFarm_Id(userId: UUID, farmId: UUID): UserFarmProfile

    fun findByUsuario_IdAndIsDefaultTrue(userId: UUID): UserFarmProfile?

}