package com.iboi.identity.application.usecase

import com.iboi.identity.api.dto.response.LoginResponse
import com.iboi.identity.infrastructure.repository.UserFarmProfileRepository
import com.iboi.identity.infrastructure.repository.UsuarioRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.util.*

@Service
class AuthenticateUserUseCase(
        private val userRepo: UsuarioRepository,
        private val ufpRepo: UserFarmProfileRepository,
        private val passwordEncoder: PasswordEncoder,
        private val buildAuthResponseUseCase: BuildAuthResponseUseCase
) {

    fun execute2(email: String, password: String): UUID {
        val user = userRepo.findByEmail(email)
                ?: throw RuntimeException("Invalid credentials")

        if (!passwordEncoder.matches(password, user.senhaHash)) {
            throw RuntimeException("Invalid credentials")
        }

        return user.id!!
    }

    fun execute(email: String, password: String): LoginResponse {
        val user = userRepo.findByEmail(email)
                ?: throw RuntimeException("Invalid credentials")

        if (!passwordEncoder.matches(password, user.senhaHash)) {
            throw RuntimeException("Invalid credentials")
        }

        val profile = ufpRepo.findByUsuario_IdAndIsDefaultTrue(user.id!!)
                ?: throw RuntimeException("User has no default farm")

        return buildAuthResponseUseCase.execute(user, profile)
    }
}
