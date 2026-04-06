package com.iboi.financeiro.api

import com.iboi.financeiro.api.dto.ReceitaDto
import com.iboi.financeiro.api.dto.RegistrarReceitaRequest
import com.iboi.financeiro.api.dto.ResumoReceitasPorTipo
import com.iboi.financeiro.domain.TipoReceita
import com.iboi.financeiro.repository.ReceitaRepository
import com.iboi.financeiro.usecase.RegistrarReceitaUseCase
import com.iboi.plano.model.PlanoRecurso
import com.iboi.plano.service.PlanoAcessoService
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
import java.time.LocalDate

@RestController
@RequestMapping("/api/receitas")
@Tag(name = "Receitas", description = "Gestao de entradas financeiras e vendas da fazenda")
class ReceitaController(
        private val planoAcessoService: PlanoAcessoService,
        private val registrarReceitaUseCase: RegistrarReceitaUseCase,
        private val receitaRepository: ReceitaRepository
) {

    @PostMapping
    @Operation(summary = "Registrar receita")
    fun registrar(@RequestBody request: RegistrarReceitaRequest): ResponseEntity<ReceitaDto> {
        planoAcessoService.requireRecurso(
                SecurityUtils.currentEmpresaId(),
                PlanoRecurso.FINANCEIRO_POR_ANIMAL,
                "O controle financeiro detalhado sera liberado nas proximas camadas pagas."
        )
        val receita = registrarReceitaUseCase.execute(
                SecurityUtils.currentFarmId(),
                SecurityUtils.currentEmail(),
                request
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(receita)
    }

    @GetMapping
    @Operation(summary = "Listar receitas")
    fun listar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) dataInicio: LocalDate?,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) dataFim: LocalDate?
    ): ResponseEntity<List<ReceitaDto>> {
        planoAcessoService.requireRecurso(
                SecurityUtils.currentEmpresaId(),
                PlanoRecurso.FINANCEIRO_POR_ANIMAL,
                "O controle financeiro detalhado sera liberado nas proximas camadas pagas."
        )

        val receitas = if (dataInicio != null && dataFim != null) {
            receitaRepository.findByFarmIdAndDataBetween(SecurityUtils.currentFarmId(), dataInicio, dataFim)
        } else {
            receitaRepository.findByFarmIdOrderByDataDesc(SecurityUtils.currentFarmId())
        }

        return ResponseEntity.ok(receitas.map {
            ReceitaDto(
                    id = it.id!!,
                    tipo = it.tipo,
                    descricao = it.descricao,
                    valor = it.valor,
                    data = it.data,
                    formaPagamento = it.formaPagamento,
                    comprador = it.comprador,
                    quantidadeAnimais = it.quantidadeAnimais,
                    responsavel = it.responsavel?.nome,
                    observacoes = it.observacoes
            )
        })
    }

    @GetMapping("/resumo-por-tipo")
    @Operation(summary = "Resumo de receitas por tipo")
    fun resumoPorTipo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) dataInicio: LocalDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) dataFim: LocalDate
    ): ResponseEntity<List<ResumoReceitasPorTipo>> {
        planoAcessoService.requireRecurso(
                SecurityUtils.currentEmpresaId(),
                PlanoRecurso.CUSTO_POR_CABECA,
                "O resumo avancado de receitas sera liberado nas proximas camadas pagas."
        )

        val resumo = receitaRepository.sumByFarmIdAndDataBetweenGroupByTipo(
                SecurityUtils.currentFarmId(),
                dataInicio,
                dataFim
        ).map {
            ResumoReceitasPorTipo(
                    tipo = it[0] as TipoReceita,
                    total = it[1] as java.math.BigDecimal
            )
        }

        return ResponseEntity.ok(resumo)
    }
}
