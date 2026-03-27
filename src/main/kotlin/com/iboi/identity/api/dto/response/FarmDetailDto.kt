package com.iboi.identity.api.dto.response

import com.iboi.identity.domain.ProductionType
import java.util.UUID

data class FarmDetailDto(
        val id: UUID,
        val name: String,
        val city: String,
        val state: String,
        val productionType: ProductionType,
        val size: Double?,
        val ownerName: String?,
        val ownerDocument: String?,
        val phone: String?,
        val email: String?,
        val addressLine: String?,
        val zipCode: String?,
        val latitude: Double?,
        val longitude: Double?,
        val legalStatus: String?,
        val documentProof: String?,
        val ccir: String?,
        val cib: String?,
        val car: String?,
        val mainExploration: String?,
        val estimatedCapacity: Int?,
        val grazingArea: Double?,
        val legalReserveArea: Double?,
        val appArea: Double?,
        val productiveArea: Double?,
        val active: Boolean
)

data class PastureDto(
        val id: UUID,
        val name: String,
        val areaHa: Double?,
        val latitude: Double?,
        val longitude: Double?,
        val geoJson: String?,
        val notes: String?,
        val active: Boolean
)
