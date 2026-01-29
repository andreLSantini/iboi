package com.iboi.identity.application.usecase

import com.iboi.identity.domain.Profile
import com.iboi.identity.infrastructure.repository.PermissionRepository
import com.iboi.identity.infrastructure.repository.ProfileRepository
import org.springframework.stereotype.Service

@Service
class CreateProfileUseCase(
        private val profileRepo: ProfileRepository,
        private val permissionRepo: PermissionRepository
) {
    fun execute(name: String, permissionCodes: Set<String>): Profile {
        val permissions = permissionCodes.map {
            permissionRepo.findByCode(it)
                    ?: throw RuntimeException("Permission $it not found")
        }.toSet()

        return profileRepo.save(Profile(name = name, permissions = permissions))
    }
}
