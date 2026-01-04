package com.iboi.orders.repository

import com.iboi.orders.domain.Order
import java.util.*

interface OrderRepository {
    fun save(order: Order): Order
    fun findAll(): List<Order>
    fun findById(id: UUID): Order?
}
