package com.iboi.identity.api.dto.request

import com.iboi.identity.domain.ProductionType

data class CadastrarFazendaRequest(
        val nome: String,
        val cidade: String,
        val estado: String,
        val tipoProducao: ProductionType,
        val tamanho: Double? = null
)
