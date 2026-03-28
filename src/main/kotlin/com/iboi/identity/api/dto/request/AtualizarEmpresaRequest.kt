package com.iboi.identity.api.dto.request

data class AtualizarEmpresaRequest(
        val nome: String,
        val cnpj: String? = null
)
