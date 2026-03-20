package com.iboi.rebanho.api

import com.iboi.rebanho.api.dto.AtualizarLoteRequest
import com.iboi.rebanho.api.dto.CadastrarLoteRequest
import com.iboi.rebanho.api.dto.LoteDto
import com.iboi.rebanho.usecase.AtualizarLoteUseCase
import com.iboi.rebanho.usecase.BuscarLotePorIdUseCase
import com.iboi.rebanho.usecase.CadastrarLoteUseCase
import com.iboi.rebanho.usecase.DeletarLoteUseCase
import com.iboi.rebanho.usecase.ListarLotesUseCase
import com.iboi.shared.security.SecurityUtils
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/lotes")
@Tag(name = "Lotes", description = "Gestao de lotes do rebanho")
class LoteController(
        private val cadastrarLoteUseCase: CadastrarLoteUseCase,
        private val listarLotesUseCase: ListarLotesUseCase,
        private val buscarLotePorIdUseCase: BuscarLotePorIdUseCase,
        private val atualizarLoteUseCase: AtualizarLoteUseCase,
        private val deletarLoteUseCase: DeletarLoteUseCase
) {

    @PostMapping
    @Operation(summary = "Cadastrar lote", description = "Cria um novo lote para organizar o rebanho")
    @ApiResponses(
            value = [
                ApiResponse(responseCode = "201", description = "Lote cadastrado com sucesso"),
                ApiResponse(responseCode = "409", description = "Nome do lote ja existe"),
                ApiResponse(responseCode = "400", description = "Dados invalidos")
            ]
    )
    fun cadastrar(@Valid @RequestBody request: CadastrarLoteRequest): ResponseEntity<LoteDto> {
        val lote = cadastrarLoteUseCase.execute(SecurityUtils.currentFarmId(), request)
        return ResponseEntity.status(HttpStatus.CREATED).body(lote)
    }

    @GetMapping
    @Operation(summary = "Listar lotes", description = "Lista todos os lotes da fazenda com paginacao")
    fun listar(
            @RequestParam(required = false) apenasAtivos: Boolean?,
            @PageableDefault(size = 20, sort = ["criadoEm"], direction = Sort.Direction.DESC) pageable: Pageable
    ): ResponseEntity<Page<LoteDto>> {
        val lotes = listarLotesUseCase.execute(SecurityUtils.currentFarmId(), apenasAtivos, pageable)
        return ResponseEntity.ok(lotes)
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar lote por ID", description = "Retorna detalhes de um lote especifico")
    fun buscarPorId(@PathVariable id: UUID): ResponseEntity<LoteDto> {
        val lote = buscarLotePorIdUseCase.execute(id, SecurityUtils.currentFarmId())
        return ResponseEntity.ok(lote)
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar lote", description = "Atualiza dados de um lote")
    fun atualizar(
            @PathVariable id: UUID,
            @Valid @RequestBody request: AtualizarLoteRequest
    ): ResponseEntity<LoteDto> {
        val lote = atualizarLoteUseCase.execute(id, SecurityUtils.currentFarmId(), request)
        return ResponseEntity.ok(lote)
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar lote", description = "Remove um lote (apenas se estiver vazio)")
    fun deletar(@PathVariable id: UUID): ResponseEntity<Void> {
        deletarLoteUseCase.execute(id, SecurityUtils.currentFarmId())
        return ResponseEntity.noContent().build()
    }
}
