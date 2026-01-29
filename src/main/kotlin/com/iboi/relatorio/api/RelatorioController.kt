package com.iboi.relatorio.api

import com.iboi.identity.infrastructure.repository.UsuarioRepository
import com.iboi.relatorio.dto.*
import com.iboi.relatorio.usecase.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.util.*

@RestController
@RequestMapping("/api/relatorios")
@Tag(name = "Relatórios", description = "Relatórios e dashboards da fazenda")
class RelatorioController(
        private val relatorioRebanhoUseCase: RelatorioRebanhoUseCase,
        private val relatorioFinanceiroUseCase: RelatorioFinanceiroUseCase,
        private val historicoAnimalUseCase: HistoricoAnimalUseCase,
        private val dashboardUseCase: DashboardUseCase,
        private val usuarioRepository: UsuarioRepository
) {

    @GetMapping("/rebanho")
    @Operation(summary = "Relatório do rebanho", description = "Totais por categoria, sexo, status, idade e peso médios")
    fun relatorioRebanho(): ResponseEntity<RelatorioRebanhoResponse> {
        val farmId = getFarmIdFromAuth()
        val relatorio = relatorioRebanhoUseCase.execute(farmId)
        return ResponseEntity.ok(relatorio)
    }

    @GetMapping("/financeiro")
    @Operation(summary = "Relatório financeiro", description = "Despesas por período e categoria")
    fun relatorioFinanceiro(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) dataInicio: LocalDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) dataFim: LocalDate
    ): ResponseEntity<RelatorioFinanceiroResponse> {
        val farmId = getFarmIdFromAuth()
        val relatorio = relatorioFinanceiroUseCase.execute(farmId, dataInicio, dataFim)
        return ResponseEntity.ok(relatorio)
    }

    @GetMapping("/historico-animal/{id}")
    @Operation(summary = "Histórico completo do animal", description = "Timeline, evolução de peso e todos os eventos")
    fun historicoAnimal(@PathVariable id: UUID): ResponseEntity<HistoricoAnimalResponse> {
        val historico = historicoAnimalUseCase.execute(id)
        return ResponseEntity.ok(historico)
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Dashboard principal", description = "KPIs, eventos recentes e agendamentos próximos")
    fun dashboard(): ResponseEntity<DashboardResponse> {
        val farmId = getFarmIdFromAuth()
        val dashboard = dashboardUseCase.execute(farmId)
        return ResponseEntity.ok(dashboard)
    }

    private fun getFarmIdFromAuth(): UUID {
        val email = SecurityContextHolder.getContext().authentication.principal as String
        val usuario = usuarioRepository.findByEmail(email)
                ?: throw IllegalStateException("Usuário não encontrado")
        return usuario.empresa.id!!
    }
}
