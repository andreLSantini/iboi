package com.iboi.identity.application.usecase

import com.iboi.identity.domain.UserFarmProfile
import com.iboi.identity.infrastructure.repository.UserFarmProfileRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class ListUserFarmsUseCase(
        private val ufpRepo: UserFarmProfileRepository
) {

    fun execute(userId: UUID): List<UserFarmProfile> =
            ufpRepo.findAllByUsuario_Id(userId)
}