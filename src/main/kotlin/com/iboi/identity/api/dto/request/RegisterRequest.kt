package com.iboi.identity.api.dto.request

data class RegisterRequest(
        val email: String,
        val senha: String,
        val nomeEmpresa: String
)