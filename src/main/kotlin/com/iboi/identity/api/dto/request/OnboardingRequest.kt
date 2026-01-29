package com.iboi.identity.api.dto.request

import com.iboi.identity.domain.EmpresaType
import com.iboi.identity.domain.ProductionType

data class OnboardingRequest(
        // Dados do Usuário
        val nome: String,
        val email: String,
        val telefone: String? = null,
        val senha: String,

        // Dados da Empresa
        val nomeEmpresa: String,
        val tipoEmpresa: EmpresaType = EmpresaType.MATRIZ,
        val cnpj: String? = null,

        // Dados da Fazenda (obrigatório)
        val nomeFazenda: String,
        val cidade: String,
        val estado: String,
        val tipoProdução: ProductionType,
        val tamanho: Double? = null
)