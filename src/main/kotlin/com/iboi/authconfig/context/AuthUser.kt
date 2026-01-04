package com.iboi.authconfig.context

data class AuthUser(
        val id: String,
        val email: String,
        val roles: List<String>,
        val permissions: List<String>,
        val tenantId: String?
)