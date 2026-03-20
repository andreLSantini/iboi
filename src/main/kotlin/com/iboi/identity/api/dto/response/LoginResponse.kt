package com.iboi.identity.api.dto.response

import com.iboi.identity.domain.FarmRole
import com.iboi.identity.domain.RoleEnum
import java.util.*

data class LoginResponse(
        val accessToken: String,
        val usuario: UsuarioDto,
        val fazenda: FazendaDto,
        val farms: List<FarmSummaryDto>,
        val defaultFarmId: UUID
)

data class UsuarioDto(
        val id: UUID,
        val nome: String,
        val email: String,
        val role: RoleEnum,
        val farmRole: FarmRole
)

data class FazendaDto(
        val id: UUID,
        val nome: String,
        val cidade: String,
        val estado: String
)
