package com.iboi.orders.dto

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive

data class CreateOrderRequest(
        @field:NotNull
        val customerId: String,

        @field:Positive
        val amount: Double
)