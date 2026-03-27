package com.iboi.identity.api.dto.request

import com.iboi.identity.domain.ProductionType

data class AtualizarFazendaRequest(
        val nome: String,
        val cidade: String,
        val estado: String,
        val tipoProducao: ProductionType,
        val tamanho: Double? = null,
        val ownerName: String? = null,
        val ownerDocument: String? = null,
        val phone: String? = null,
        val email: String? = null,
        val addressLine: String? = null,
        val zipCode: String? = null,
        val latitude: Double? = null,
        val longitude: Double? = null,
        val legalStatus: String? = null,
        val documentProof: String? = null,
        val ccir: String? = null,
        val cib: String? = null,
        val car: String? = null,
        val mainExploration: String? = null,
        val estimatedCapacity: Int? = null,
        val grazingArea: Double? = null,
        val legalReserveArea: Double? = null,
        val appArea: Double? = null,
        val productiveArea: Double? = null
)
