package com.iboi.ia.api

import com.iboi.ia.usecase.DashboardInteligenteResponse
import com.iboi.ia.usecase.DashboardInteligenteUseCase
import com.iboi.identity.infrastructure.repository.UsuarioRepository
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/ia")
@Tag(name = "IA Dashboard", description = "Dashboard inteligente com predições e análises")
class DashboardInteligenteController(
        private val dashboardInteligenteUseCase: DashboardInteligenteUseCase,
        private val usuarioRepository: UsuarioRepository
) {

    @GetMapping("/dashboard")
    @Operation(
            summary = "Dashboard inteligente",
            description = "Retorna análises preditivas, scores de risco, alertas e recomendações baseadas em IA"
    )
    fun getDashboard(): ResponseEntity<DashboardInteligenteResponse> {
        val farmId = getFarmIdFromAuth()
        val dashboard = dashboardInteligenteUseCase.execute(farmId)
        return ResponseEntity.ok(dashboard)
    }

    private fun getFarmIdFromAuth(): UUID {
        val email = SecurityContextHolder.getContext().authentication.principal as String
        val usuario = usuarioRepository.findByEmail(email)
                ?: throw IllegalStateException("Usuário não encontrado")
        return usuario.empresa.id!!
    }
}
