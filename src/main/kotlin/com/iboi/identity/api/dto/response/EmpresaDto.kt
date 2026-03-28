package com.iboi.identity.api.dto.response

import com.iboi.identity.domain.EmpresaType
import java.util.UUID

data class EmpresaDto(
        val id: UUID,
        val nome: String,
        val tipo: EmpresaType,
        val cnpj: String? = null,
        val asaasCustomerId: String? = null,
        val ativa: Boolean
)
