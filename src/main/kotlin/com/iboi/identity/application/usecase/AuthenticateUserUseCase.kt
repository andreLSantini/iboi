package com.iboi.identity.application.usecase

import com.iboi.identity.api.dto.response.FarmSummaryDto
import com.iboi.identity.api.dto.response.FazendaDto
import com.iboi.identity.api.dto.response.LoginResponse
import com.iboi.identity.api.dto.response.UsuarioDto
import com.iboi.identity.application.service.JwtService
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
        private val jwtService: JwtService,
        private val resolvePermissionsUseCase: ResolvePermissionsUseCase
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

        val permissions = resolvePermissionsUseCase.execute(profile.role)

        val token = jwtService.generateToken(
                user = user,
                permissions = permissions,
                defaultFarmId = profile.farm.id
        )

        val farms = ufpRepo.findAllByUsuario_Id(user.id!!)
                .map {
                    FarmSummaryDto(
                            id = it.farm.id!!,
                            name = it.farm.name
                    )
                }

        return LoginResponse(
                accessToken = token,
                usuario = UsuarioDto(
                        id = user.id!!,
                        nome = user.nome,
                        email = user.email,
                        role = user.roleEnum,
                        farmRole = profile.role
                ),
                fazenda = FazendaDto(
                        id = profile.farm.id!!,
                        nome = profile.farm.name,
                        cidade = profile.farm.city,
                        estado = profile.farm.state
                ),
                farms = farms,
                defaultFarmId = profile.farm.id!!
        )
    }
}
