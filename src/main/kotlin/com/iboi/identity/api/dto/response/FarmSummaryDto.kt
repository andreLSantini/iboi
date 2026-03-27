package com.iboi.identity.api.dto.response

import java.util.*

data class FarmSummaryDto(
        val id: UUID,
        val name: String,
        val city: String? = null,
        val state: String? = null,
        val productionType: String? = null,
        val size: Double? = null,
        val active: Boolean = true,
        val pastureCount: Int = 0
)
