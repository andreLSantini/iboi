package com.iboi.orders.service

import com.iboi.orders.domain.Order
import com.iboi.orders.dto.CreateOrderRequest
import com.iboi.orders.repository.OrderRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class OrderService(
        private val repository: OrderRepository
) {

    fun create(request: CreateOrderRequest): Order {
        val order = Order(
                customerId = request.customerId,
                amount = request.amount
        )
        return repository.save(order)
    }

    fun list(): List<Order> = repository.findAll()

    fun get(id: UUID): Order =
            repository.findById(id)
                    ?: throw IllegalArgumentException("Order not found")
}