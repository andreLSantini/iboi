package com.iboi.identity.application.usecase

import com.iboi.identity.api.dto.response.FarmSummaryDto
import com.iboi.identity.api.dto.response.FazendaDto
import com.iboi.identity.api.dto.response.LoginResponse
import com.iboi.identity.api.dto.response.UsuarioDto
import com.iboi.identity.application.service.JwtService
import com.iboi.identity.domain.UserFarmProfile
import com.iboi.identity.domain.Usuario
import com.iboi.identity.infrastructure.repository.UserFarmProfileRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class BuildAuthResponseUseCase(
        private val jwtService: JwtService,
        private val resolvePermissionsUseCase: ResolvePermissionsUseCase,
        private val userFarmProfileRepository: UserFarmProfileRepository
) {

    fun execute(user: Usuario, selectedProfile: UserFarmProfile): LoginResponse {
        val permissions = resolvePermissionsUseCase.execute(selectedProfile.role)
        val accessToken = jwtService.generateToken(
                user = user,
                permissions = permissions,
                defaultFarmId = selectedProfile.farm.id
        )

        val farms = userFarmProfileRepository.findAllByUsuario_Id(user.id!!)
                .map {
                    FarmSummaryDto(
                            id = it.farm.id!!,
                            name = it.farm.name
                    )
                }

        return LoginResponse(
                accessToken = accessToken,
                usuario = UsuarioDto(
                        id = user.id!!,
                        nome = user.nome,
                        email = user.email,
                        role = user.roleEnum,
                        farmRole = selectedProfile.role
                ),
                fazenda = FazendaDto(
                        id = selectedProfile.farm.id!!,
                        nome = selectedProfile.farm.name,
                        cidade = selectedProfile.farm.city,
                        estado = selectedProfile.farm.state
                ),
                farms = farms,
                defaultFarmId = selectedProfile.farm.id!!
        )
    }

    fun execute(userId: UUID, selectedFarmId: UUID): LoginResponse {
        val profile = userFarmProfileRepository.findByUsuario_IdAndFarm_Id(userId, selectedFarmId)
                ?: throw RuntimeException("Usuário não possui acesso a esta fazenda")

        return execute(profile.usuario, profile)
    }
}
