package com.iboi.relatorio.api

import com.iboi.relatorio.dto.DashboardResponse
import com.iboi.relatorio.dto.HistoricoAnimalResponse
import com.iboi.relatorio.dto.RelatorioFinanceiroResponse
import com.iboi.relatorio.dto.RelatorioRebanhoResponse
import com.iboi.relatorio.usecase.DashboardUseCase
import com.iboi.relatorio.usecase.HistoricoAnimalUseCase
import com.iboi.relatorio.usecase.RelatorioFinanceiroUseCase
import com.iboi.relatorio.usecase.RelatorioRebanhoUseCase
import com.iboi.shared.security.SecurityUtils
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.util.UUID

@RestController
@RequestMapping("/api/relatorios")
@Tag(name = "Relatorios", description = "Relatorios e dashboards da fazenda")
class RelatorioController(
        private val relatorioRebanhoUseCase: RelatorioRebanhoUseCase,
        private val relatorioFinanceiroUseCase: RelatorioFinanceiroUseCase,
        private val historicoAnimalUseCase: HistoricoAnimalUseCase,
        private val dashboardUseCase: DashboardUseCase
) {

    @GetMapping("/rebanho")
    @Operation(summary = "Relatorio do rebanho", description = "Totais por categoria, sexo, status, idade e peso medios")
    fun relatorioRebanho(): ResponseEntity<RelatorioRebanhoResponse> {
        return ResponseEntity.ok(relatorioRebanhoUseCase.execute(SecurityUtils.currentFarmId()))
    }

    @GetMapping("/financeiro")
    @Operation(summary = "Relatorio financeiro", description = "Despesas por periodo e categoria")
    fun relatorioFinanceiro(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) dataInicio: LocalDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) dataFim: LocalDate
    ): ResponseEntity<RelatorioFinanceiroResponse> {
        val relatorio = relatorioFinanceiroUseCase.execute(SecurityUtils.currentFarmId(), dataInicio, dataFim)
        return ResponseEntity.ok(relatorio)
    }

    @GetMapping("/historico-animal/{id}")
    @Operation(summary = "Historico completo do animal", description = "Timeline, evolucao de peso e todos os eventos")
    fun historicoAnimal(@PathVariable id: UUID): ResponseEntity<HistoricoAnimalResponse> {
        return ResponseEntity.ok(historicoAnimalUseCase.execute(id))
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Dashboard principal", description = "KPIs, eventos recentes e agendamentos proximos")
    fun dashboard(): ResponseEntity<DashboardResponse> {
        return ResponseEntity.ok(dashboardUseCase.execute(SecurityUtils.currentFarmId()))
    }
}
