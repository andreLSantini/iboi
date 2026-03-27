package com.iboi.identity.application.usecase

import com.iboi.identity.application.service.JwtService
import com.iboi.identity.infrastructure.repository.UserFarmProfileRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class GenerateFarmTokenUseCase(
        private val ufpRepo: UserFarmProfileRepository,
        private val jwtService: JwtService,
        private val resolvePermissionsUseCase: ResolvePermissionsUseCase
) {

    fun execute(userId: UUID, farmId: UUID): String {
        val profile = ufpRepo.findByUsuario_IdAndFarm_Id(userId, farmId)
                ?: throw RuntimeException("User has no access to this farm")

        val permissions = resolvePermissionsUseCase.execute(profile.role)

        return jwtService.generateToken(
                user = profile.usuario,
                permissions = permissions,
                defaultFarmId = farmId
        )
    }
}
