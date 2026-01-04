package com.iboi.auth.dto.request

data class LoginRequest(
        val email: String,
        val senha: String
)