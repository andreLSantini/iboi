package com.iboi.orders.repository

import com.iboi.orders.domain.Order
import org.springframework.stereotype.Repository
import java.util.*
import kotlin.collections.MutableCollection.*

@Repository
class InMemoryOrderRepository : OrderRepository {

    private val orders = mutableMapOf<UUID, Order>()

    override fun save(order: Order): Order {
        orders[order.id] = order
        return order
    }

    override fun findAll(): List<Order> = orders.values.toList()

    override fun findById(id: UUID): Order? = orders[id]
}
