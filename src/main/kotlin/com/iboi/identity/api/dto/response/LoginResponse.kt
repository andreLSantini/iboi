package com.iboi.identity.api.dto.response

import java.util.*

data class LoginResponse(
        val accessToken: String,
        val farms: List<FarmSummaryDto>,
        val defaultFarmId: UUID
)