package com.iboi.orders.controller

import com.iboi.orders.domain.toResponse
import com.iboi.orders.dto.CreateOrderRequest
import com.iboi.orders.dto.OrderResponse
import com.iboi.orders.service.OrderService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.UUID
import java.util.stream.Stream

@RestController
@RequestMapping("/orders")
@Tag(name = "Orders", description = "Order management")
@SecurityRequirement(name = "bearerAuth")
class OrderController(
        private val service: OrderService
) {

    @Operation(summary = "Create a new order")
    @PreAuthorize("hasAuthority('ORDER_WRITE')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
            @Valid @RequestBody request: CreateOrderRequest
    ): OrderResponse =
            service.create(request).toResponse()

    @Operation(summary = "List all orders")
    @PreAuthorize("hasAuthority('ORDER_READ')")
    @GetMapping
    fun list(): Stream<OrderResponse>? =
           service.list().stream().map { it.toResponse() }

    @Operation(summary = "Get order by ID")
    @PreAuthorize("hasAuthority('ORDER_READ')")
    @GetMapping("/{id}")
    fun get(@PathVariable id: UUID): OrderResponse =
            service.get(id).toResponse()
}
