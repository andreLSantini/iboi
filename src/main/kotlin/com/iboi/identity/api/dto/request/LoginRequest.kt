package com.iboi.identity.api.dto.request

data class LoginRequest(
        val email: String,
        val senha: String
)