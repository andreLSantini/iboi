package com.iboi.orders.dto

import com.iboi.orders.domain.OrderStatus
import java.time.Instant
import java.util.UUID

data class OrderResponse(
        val id: UUID,
        val customerId: String,
        val amount: Double,
        val status: OrderStatus,
        val createdAt: Instant
)