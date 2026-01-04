package com.iboi.auth.dto.request

import com.iboi.auth.model.TipoPessoa
import java.util.*

data class SignupRequest(
        val nomePessoa: String,
        val tipoPessoa: TipoPessoa,
        val documento: String,
        val email: String,
        val senha: String,
        val nomeEmpresa: String,
        val empresaPaiId: UUID? = null
)