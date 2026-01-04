package com.iboi.orders.domain

import com.iboi.orders.dto.OrderResponse
import java.time.Instant
import java.util.UUID

data class Order(
        val id: UUID = UUID.randomUUID(),
        val customerId: String,
        val amount: Double,
        val status: OrderStatus = OrderStatus.CREATED,
        val createdAt: Instant = Instant.now()


)
fun Order.toResponse() = OrderResponse(
        id = id,
        customerId = customerId,
        amount = amount,
        status = status,
        createdAt = createdAt
)
