package com.iboi.ia.api

import com.iboi.plano.model.PlanoRecurso
import com.iboi.plano.service.PlanoAcessoService
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
@Tag(name = "IA Dashboard", description = "Dashboard heurístico com scores, predicoes simples e recomendacoes")
class DashboardInteligenteController(
        private val planoAcessoService: PlanoAcessoService,
        private val dashboardInteligenteUseCase: DashboardInteligenteUseCase
) {

    @GetMapping("/dashboard")
    @Operation(
            summary = "Dashboard inteligente",
            description = "Retorna analises preditivas, scores de risco, alertas e recomendacoes baseadas em IA"
    )
    fun getDashboard(): ResponseEntity<DashboardInteligenteResponse> {
        planoAcessoService.requireRecurso(
                SecurityUtils.currentEmpresaId(),
                PlanoRecurso.IA_DECISAO,
                "A IA assistida do BovCore precisa estar habilitada no plano para mostrar insights operacionais."
        )
        val dashboard = dashboardInteligenteUseCase.execute(SecurityUtils.currentFarmId())
        return ResponseEntity.ok(dashboard)
    }
}
