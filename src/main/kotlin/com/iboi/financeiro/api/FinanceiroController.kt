package com.iboi.financeiro.api

import com.iboi.financeiro.api.dto.FluxoCaixaDto
import com.iboi.financeiro.usecase.BuscarFluxoCaixaUseCase
import com.iboi.plano.model.PlanoRecurso
import com.iboi.plano.service.PlanoAcessoService
import com.iboi.shared.security.SecurityUtils
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@RequestMapping("/api/financeiro")
@Tag(name = "Financeiro", description = "Fluxo de caixa, contas a pagar e contas a receber")
class FinanceiroController(
        private val planoAcessoService: PlanoAcessoService,
        private val buscarFluxoCaixaUseCase: BuscarFluxoCaixaUseCase
) {

    @GetMapping("/fluxo-caixa")
    @Operation(summary = "Consultar fluxo de caixa")
    fun fluxoCaixa(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) dataInicio: LocalDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) dataFim: LocalDate
    ): ResponseEntity<FluxoCaixaDto> {
        planoAcessoService.requireRecurso(
                SecurityUtils.currentEmpresaId(),
                PlanoRecurso.FINANCEIRO_POR_ANIMAL,
                "O fluxo de caixa detalhado sera liberado nas proximas camadas pagas."
        )

        return ResponseEntity.ok(
                buscarFluxoCaixaUseCase.execute(SecurityUtils.currentFarmId(), dataInicio, dataFim)
        )
    }
}
