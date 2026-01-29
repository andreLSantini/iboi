package com.iboi.identity.application.usecase

import com.iboi.identity.domain.FarmRole
import com.iboi.identity.infrastructure.repository.RolePermissionRepository
import com.iboi.identity.infrastructure.repository.UserFarmProfileRepository
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class ResolvePermissionsUseCase(
        private val userFarmProfileRepository: UserFarmProfileRepository,
        private val rolePermissionRepository: RolePermissionRepository
) {

    fun execute(role: FarmRole): List<String> =
            when (role) {
                FarmRole.ADMIN -> listOf(
                        "FARM_READ",
                        "FARM_WRITE",
                        "ANIMAL_READ",
                        "ANIMAL_WRITE",
                        "USER_MANAGE",
                        "BILLING_MANAGE"
                )

                FarmRole.MANAGER -> listOf(
                        "FARM_READ",
                        "ANIMAL_READ",
                        "ANIMAL_WRITE"
                )

                FarmRole.OPERATOR -> listOf(
                        "ANIMAL_READ",
                        "ANIMAL_WRITE"
                )

                FarmRole.VIEWER -> listOf(
                        "ANIMAL_READ"
                )
            }
}
