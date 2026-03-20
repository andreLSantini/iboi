package com.iboi.rebanho.api

import com.iboi.rebanho.api.dto.AnimalDto
import com.iboi.rebanho.api.dto.AtualizarAnimalRequest
import com.iboi.rebanho.api.dto.CadastrarAnimalRequest
import com.iboi.rebanho.api.dto.FiltrarAnimaisRequest
import com.iboi.rebanho.domain.CategoriaAnimal
import com.iboi.rebanho.domain.Sexo
import com.iboi.rebanho.domain.StatusAnimal
import com.iboi.rebanho.usecase.AtualizarAnimalUseCase
import com.iboi.rebanho.usecase.BuscarAnimalPorIdUseCase
import com.iboi.rebanho.usecase.CadastrarAnimalUseCase
import com.iboi.rebanho.usecase.DeletarAnimalUseCase
import com.iboi.rebanho.usecase.ListarAnimaisUseCase
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
@RequestMapping("/api/animais")
@Tag(name = "Animais", description = "Gestao do rebanho")
class AnimalController(
        private val cadastrarAnimalUseCase: CadastrarAnimalUseCase,
        private val listarAnimaisUseCase: ListarAnimaisUseCase,
        private val buscarAnimalPorIdUseCase: BuscarAnimalPorIdUseCase,
        private val atualizarAnimalUseCase: AtualizarAnimalUseCase,
        private val deletarAnimalUseCase: DeletarAnimalUseCase
) {

    @PostMapping
    @Operation(summary = "Cadastrar animal", description = "Cadastra um novo animal no rebanho")
    @ApiResponses(
            value = [
                ApiResponse(responseCode = "201", description = "Animal cadastrado com sucesso"),
                ApiResponse(responseCode = "409", description = "Brinco ja existe"),
                ApiResponse(responseCode = "400", description = "Dados invalidos")
            ]
    )
    fun cadastrar(@Valid @RequestBody request: CadastrarAnimalRequest): ResponseEntity<AnimalDto> {
        val animal = cadastrarAnimalUseCase.execute(SecurityUtils.currentFarmId(), request)
        return ResponseEntity.status(HttpStatus.CREATED).body(animal)
    }

    @GetMapping
    @Operation(summary = "Listar animais", description = "Lista animais com filtros e paginacao")
    fun listar(
            @RequestParam(required = false) status: StatusAnimal?,
            @RequestParam(required = false) categoria: CategoriaAnimal?,
            @RequestParam(required = false) loteId: UUID?,
            @RequestParam(required = false) sexo: Sexo?,
            @PageableDefault(size = 20, sort = ["criadoEm"], direction = Sort.Direction.DESC) pageable: Pageable
    ): ResponseEntity<Page<AnimalDto>> {
        val filtro = FiltrarAnimaisRequest(status, categoria, loteId, sexo)
        val animais = listarAnimaisUseCase.execute(SecurityUtils.currentFarmId(), filtro, pageable)
        return ResponseEntity.ok(animais)
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar animal por ID", description = "Retorna detalhes de um animal especifico")
    fun buscarPorId(@PathVariable id: UUID): ResponseEntity<AnimalDto> {
        val animal = buscarAnimalPorIdUseCase.execute(id, SecurityUtils.currentFarmId())
        return ResponseEntity.ok(animal)
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar animal", description = "Atualiza dados de um animal")
    fun atualizar(
            @PathVariable id: UUID,
            @Valid @RequestBody request: AtualizarAnimalRequest
    ): ResponseEntity<AnimalDto> {
        val animal = atualizarAnimalUseCase.execute(id, SecurityUtils.currentFarmId(), request)
        return ResponseEntity.ok(animal)
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar animal", description = "Remove um animal do rebanho (soft delete)")
    fun deletar(@PathVariable id: UUID): ResponseEntity<Void> {
        deletarAnimalUseCase.execute(id, SecurityUtils.currentFarmId())
        return ResponseEntity.noContent().build()
    }
}
