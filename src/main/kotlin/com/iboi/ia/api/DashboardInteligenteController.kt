package com.iboi.ia.api

import com.iboi.ia.usecase.DashboardInteligenteResponse
import com.iboi.ia.usecase.DashboardInteligenteUseCase
import com.iboi.shared.security.SecurityUtils
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/ia")
@Tag(name = "IA Dashboard", description = "Dashboard inteligente com predicoes e analises")
class DashboardInteligenteController(
        private val dashboardInteligenteUseCase: DashboardInteligenteUseCase
) {

    @GetMapping("/dashboard")
    @Operation(
            summary = "Dashboard inteligente",
            description = "Retorna analises preditivas, scores de risco, alertas e recomendacoes baseadas em IA"
    )
    fun getDashboard(): ResponseEntity<DashboardInteligenteResponse> {
        val dashboard = dashboardInteligenteUseCase.execute(SecurityUtils.currentFarmId())
        return ResponseEntity.ok(dashboard)
    }
}
