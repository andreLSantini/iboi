package com.iboi.identity.api.dto.request

data class CadastrarPastoRequest(
        val nome: String,
        val areaHa: Double? = null,
        val latitude: Double,
        val longitude: Double,
        val geoJson: String? = null,
        val notes: String? = null
)
