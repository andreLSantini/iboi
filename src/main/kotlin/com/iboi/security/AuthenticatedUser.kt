package com.iboi.security

import java.util.UUID

data class AuthenticatedUser(
        val userId: UUID,
        val email: String,
        val empresaId: UUID,
        val farmId: UUID?
)
