package com.iboi.financeiro.api

import com.iboi.plano.model.PlanoRecurso
import com.iboi.plano.service.PlanoAcessoService
import com.iboi.financeiro.api.dto.DespesaDto
import com.iboi.financeiro.api.dto.RegistrarDespesaRequest
import com.iboi.financeiro.api.dto.ResumoDespesasPorCategoria
import com.iboi.financeiro.domain.CategoriaDespesa
import com.iboi.financeiro.repository.DespesaRepository
import com.iboi.financeiro.usecase.RegistrarDespesaUseCase
import com.iboi.shared.security.SecurityUtils
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

@RestController
@RequestMapping("/api/despesas")
@Tag(name = "Despesas", description = "Gestao de custos e despesas da fazenda")
class DespesaController(
        private val planoAcessoService: PlanoAcessoService,
        private val registrarDespesaUseCase: RegistrarDespesaUseCase,
        private val despesaRepository: DespesaRepository
) {

    @PostMapping
    @Operation(summary = "Registrar despesa")
    fun registrar(@RequestBody request: RegistrarDespesaRequest): ResponseEntity<DespesaDto> {
        planoAcessoService.requireRecurso(
                SecurityUtils.currentEmpresaId(),
                PlanoRecurso.FINANCEIRO_POR_ANIMAL,
                "Controle financeiro faz parte do plano Pro ou superior."
        )
        val despesa = registrarDespesaUseCase.execute(
                getFarmIdFromAuth(),
                getEmailFromAuth(),
                request
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(despesa)
    }

    @GetMapping
    @Operation(summary = "Listar despesas")
    fun listar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) dataInicio: LocalDate?,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) dataFim: LocalDate?
    ): ResponseEntity<List<DespesaDto>> {
        planoAcessoService.requireRecurso(
                SecurityUtils.currentEmpresaId(),
                PlanoRecurso.FINANCEIRO_POR_ANIMAL,
                "Controle financeiro faz parte do plano Pro ou superior."
        )
        val farmId = getFarmIdFromAuth()

        val despesas = if (dataInicio != null && dataFim != null) {
            despesaRepository.findByFarmIdAndDataBetween(farmId, dataInicio, dataFim)
        } else {
            despesaRepository.findByFarmIdOrderByDataDesc(farmId)
        }

        val dtos = despesas.map {
            DespesaDto(
                    id = it.id!!,
                    categoria = it.categoria,
                    descricao = it.descricao,
                    valor = it.valor,
                    data = it.data,
                    formaPagamento = it.formaPagamento,
                    responsavel = it.responsavel?.nome,
                    observacoes = it.observacoes
            )
        }

        return ResponseEntity.ok(dtos)
    }

    @GetMapping("/resumo-por-categoria")
    @Operation(summary = "Resumo de despesas por categoria")
    fun resumoPorCategoria(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) dataInicio: LocalDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) dataFim: LocalDate
    ): ResponseEntity<List<ResumoDespesasPorCategoria>> {
        planoAcessoService.requireRecurso(
                SecurityUtils.currentEmpresaId(),
                PlanoRecurso.CUSTO_POR_CABECA,
                "Resumo de custos faz parte do plano Pro ou superior."
        )
        val resultado = despesaRepository.sumByFarmIdAndDataBetweenGroupByCategoria(
                getFarmIdFromAuth(),
                dataInicio,
                dataFim
        )

        val resumo = resultado.map {
            ResumoDespesasPorCategoria(
                    categoria = it[0] as CategoriaDespesa,
                    total = it[1] as BigDecimal
            )
        }

        return ResponseEntity.ok(resumo)
    }

    private fun getFarmIdFromAuth(): UUID = SecurityUtils.currentFarmId()

    private fun getEmailFromAuth(): String = SecurityUtils.currentEmail()
}
