package com.iboi.relatorio.api

import com.iboi.plano.model.PlanoRecurso
import com.iboi.plano.service.PlanoAcessoService
import com.iboi.relatorio.dto.DashboardResponse
import com.iboi.relatorio.dto.HistoricoAnimalResponse
import com.iboi.relatorio.dto.RelatorioFinanceiroResponse
import com.iboi.relatorio.dto.RelatorioRebanhoResponse
import com.iboi.relatorio.service.RelatorioPdfService
import com.iboi.relatorio.usecase.DashboardUseCase
import com.iboi.relatorio.usecase.HistoricoAnimalUseCase
import com.iboi.relatorio.usecase.RelatorioFinanceiroUseCase
import com.iboi.relatorio.usecase.RelatorioRebanhoUseCase
import com.iboi.shared.security.SecurityUtils
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
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
        private val planoAcessoService: PlanoAcessoService,
        private val relatorioRebanhoUseCase: RelatorioRebanhoUseCase,
        private val relatorioFinanceiroUseCase: RelatorioFinanceiroUseCase,
        private val historicoAnimalUseCase: HistoricoAnimalUseCase,
        private val dashboardUseCase: DashboardUseCase,
        private val relatorioPdfService: RelatorioPdfService
) {

    @GetMapping("/rebanho")
    @Operation(summary = "Relatorio do rebanho", description = "Totais por categoria, sexo, status, idade e peso medios")
    fun relatorioRebanho(): ResponseEntity<RelatorioRebanhoResponse> {
        planoAcessoService.requireRecurso(
                SecurityUtils.currentEmpresaId(),
                PlanoRecurso.RELATORIOS,
                "Relatorios simples fazem parte da camada operacional atual do BovCore."
        )
        return ResponseEntity.ok(relatorioRebanhoUseCase.execute(SecurityUtils.currentFarmId()))
    }

    @GetMapping("/financeiro")
    @Operation(summary = "Relatorio financeiro", description = "Despesas por periodo e categoria")
    fun relatorioFinanceiro(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) dataInicio: LocalDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) dataFim: LocalDate
    ): ResponseEntity<RelatorioFinanceiroResponse> {
        planoAcessoService.requireRecurso(
                SecurityUtils.currentEmpresaId(),
                PlanoRecurso.FINANCEIRO_POR_ANIMAL,
                "O financeiro detalhado por animal sera liberado nas proximas camadas pagas."
        )
        val relatorio = relatorioFinanceiroUseCase.execute(SecurityUtils.currentFarmId(), dataInicio, dataFim)
        return ResponseEntity.ok(relatorio)
    }

    @GetMapping("/historico-animal/{id}")
    @Operation(summary = "Historico completo do animal", description = "Timeline, evolucao de peso e todos os eventos")
    fun historicoAnimal(@PathVariable id: UUID): ResponseEntity<HistoricoAnimalResponse> {
        planoAcessoService.requireRecurso(
                SecurityUtils.currentEmpresaId(),
                PlanoRecurso.RELATORIOS,
                "O historico consolidado faz parte da camada operacional atual do BovCore."
        )
        return ResponseEntity.ok(historicoAnimalUseCase.execute(id))
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Dashboard principal", description = "KPIs, eventos recentes e agendamentos proximos")
    fun dashboard(): ResponseEntity<DashboardResponse> {
        planoAcessoService.requireRecurso(
                SecurityUtils.currentEmpresaId(),
                PlanoRecurso.RELATORIOS,
                "O dashboard operacional faz parte da camada atual do BovCore."
        )
        return ResponseEntity.ok(dashboardUseCase.execute(SecurityUtils.currentFarmId()))
    }

    @GetMapping(value = ["/exportar/fazenda.pdf"], produces = [MediaType.APPLICATION_PDF_VALUE])
    @Operation(summary = "Exportar PDF da fazenda", description = "Gera um PDF com o resumo operacional e relatorios simples da fazenda")
    fun exportarRelatorioFazendaPdf(): ResponseEntity<ByteArray> {
        planoAcessoService.requireRecurso(
                SecurityUtils.currentEmpresaId(),
                PlanoRecurso.RELATORIOS,
                "Relatorios simples fazem parte da camada operacional atual do BovCore."
        )

        val pdf = relatorioPdfService.exportarRelatorioFazendaPdf(SecurityUtils.currentFarmId())
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=relatorio-fazenda-bovcore.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf)
    }

    @GetMapping(value = ["/exportar/animal/{id}.pdf"], produces = [MediaType.APPLICATION_PDF_VALUE])
    @Operation(summary = "Exportar PDF do animal", description = "Gera um PDF com a ficha e o historico consolidado do animal")
    fun exportarHistoricoAnimalPdf(@PathVariable id: UUID): ResponseEntity<ByteArray> {
        planoAcessoService.requireRecurso(
                SecurityUtils.currentEmpresaId(),
                PlanoRecurso.RELATORIOS,
                "O historico consolidado faz parte da camada operacional atual do BovCore."
        )

        val pdf = relatorioPdfService.exportarHistoricoAnimalPdf(id)
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=relatorio-animal-bovcore.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf)
    }
}
