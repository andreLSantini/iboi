package com.iboi.rebanho.api

import com.iboi.plano.model.PlanoRecurso
import com.iboi.plano.service.PlanoAcessoService
import com.iboi.rebanho.api.dto.AnimalDto
import com.iboi.rebanho.api.dto.AnimalFichaCompletaDto
import com.iboi.rebanho.api.dto.AtualizarAnimalRequest
import com.iboi.rebanho.api.dto.CadastrarAnimalRequest
import com.iboi.rebanho.api.dto.FiltrarAnimaisRequest
import com.iboi.rebanho.api.dto.ImportarAnimaisResponse
import com.iboi.rebanho.api.dto.MovimentacaoAnimalDto
import com.iboi.rebanho.api.dto.MovimentacaoLoteResultadoDto
import com.iboi.rebanho.api.dto.PesagemAnimalDto
import com.iboi.rebanho.api.dto.RegistrarMovimentacaoAnimalRequest
import com.iboi.rebanho.api.dto.RegistrarMovimentacaoLoteRequest
import com.iboi.rebanho.api.dto.RegistrarVacinacaoAnimalRequest
import com.iboi.rebanho.api.dto.VacinacaoAnimalDto
import com.iboi.rebanho.domain.CategoriaAnimal
import com.iboi.rebanho.domain.Sexo
import com.iboi.rebanho.domain.StatusAnimal
import com.iboi.rebanho.usecase.AtualizarAnimalUseCase
import com.iboi.rebanho.usecase.BuscarAnimalPorIdUseCase
import com.iboi.rebanho.usecase.BuscarFichaCompletaAnimalUseCase
import com.iboi.rebanho.usecase.CadastrarAnimalUseCase
import com.iboi.rebanho.usecase.DeletarAnimalUseCase
import com.iboi.rebanho.usecase.ImportarAnimaisCsvUseCase
import com.iboi.rebanho.usecase.ListarAnimaisUseCase
import com.iboi.rebanho.usecase.ListarMovimentacoesAnimalUseCase
import com.iboi.rebanho.usecase.ListarPesagensAnimalUseCase
import com.iboi.rebanho.usecase.ListarVacinacoesAnimalUseCase
import com.iboi.rebanho.usecase.RegistrarMovimentacaoAnimalUseCase
import com.iboi.rebanho.usecase.RegistrarMovimentacaoLoteUseCase
import com.iboi.rebanho.usecase.RegistrarVacinacaoAnimalUseCase
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
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

@RestController
@RequestMapping("/api/animais")
@Tag(name = "Animais", description = "Gestao do rebanho")
class AnimalController(
        private val planoAcessoService: PlanoAcessoService,
        private val cadastrarAnimalUseCase: CadastrarAnimalUseCase,
        private val listarAnimaisUseCase: ListarAnimaisUseCase,
        private val buscarAnimalPorIdUseCase: BuscarAnimalPorIdUseCase,
        private val buscarFichaCompletaAnimalUseCase: BuscarFichaCompletaAnimalUseCase,
        private val atualizarAnimalUseCase: AtualizarAnimalUseCase,
        private val deletarAnimalUseCase: DeletarAnimalUseCase,
        private val importarAnimaisCsvUseCase: ImportarAnimaisCsvUseCase,
        private val registrarMovimentacaoAnimalUseCase: RegistrarMovimentacaoAnimalUseCase,
        private val registrarMovimentacaoLoteUseCase: RegistrarMovimentacaoLoteUseCase,
        private val listarMovimentacoesAnimalUseCase: ListarMovimentacoesAnimalUseCase,
        private val listarPesagensAnimalUseCase: ListarPesagensAnimalUseCase,
        private val registrarVacinacaoAnimalUseCase: RegistrarVacinacaoAnimalUseCase,
        private val listarVacinacoesAnimalUseCase: ListarVacinacoesAnimalUseCase
) {

    @PostMapping
    @Operation(summary = "Cadastrar animal", description = "Cadastra um novo animal no rebanho")
    @ApiResponses(
            value = [
                ApiResponse(responseCode = "201", description = "Animal cadastrado com sucesso"),
                ApiResponse(responseCode = "409", description = "Identificacao duplicada"),
                ApiResponse(responseCode = "400", description = "Dados invalidos")
            ]
    )
    fun cadastrar(@Valid @RequestBody request: CadastrarAnimalRequest): ResponseEntity<AnimalDto> {
        planoAcessoService.requireCapacidadeAnimais(SecurityUtils.currentEmpresaId())
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
        return ResponseEntity.ok(listarAnimaisUseCase.execute(SecurityUtils.currentFarmId(), filtro, pageable))
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar animal por ID", description = "Retorna detalhes de um animal especifico")
    fun buscarPorId(@PathVariable id: UUID): ResponseEntity<AnimalDto> {
        return ResponseEntity.ok(buscarAnimalPorIdUseCase.execute(id, SecurityUtils.currentFarmId()))
    }

    @GetMapping("/{id}/ficha-completa")
    @Operation(summary = "Buscar ficha completa do animal", description = "Retorna a ficha operacional agregada do animal com eventos, vacinacoes e movimentacoes")
    fun buscarFichaCompleta(@PathVariable id: UUID): ResponseEntity<AnimalFichaCompletaDto> {
        return ResponseEntity.ok(buscarFichaCompletaAnimalUseCase.execute(id, SecurityUtils.currentFarmId()))
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar animal", description = "Atualiza dados de um animal")
    fun atualizar(
            @PathVariable id: UUID,
            @Valid @RequestBody request: AtualizarAnimalRequest
    ): ResponseEntity<AnimalDto> {
        return ResponseEntity.ok(atualizarAnimalUseCase.execute(id, SecurityUtils.currentFarmId(), request))
    }

    @PostMapping("/importar-csv")
    @Operation(summary = "Importar animais por CSV", description = "Importa uma planilha CSV simples para acelerar o onboarding da fazenda")
    fun importarCsv(@RequestParam("file") file: MultipartFile): ResponseEntity<ImportarAnimaisResponse> {
        planoAcessoService.requireCapacidadeAnimais(SecurityUtils.currentEmpresaId())
        return ResponseEntity.ok(importarAnimaisCsvUseCase.execute(SecurityUtils.currentFarmId(), file))
    }

    @PostMapping("/{id}/movimentacoes")
    @Operation(summary = "Registrar movimentacao do animal", description = "Cria historico formal de rastreabilidade por pasto ou fazenda")
    fun registrarMovimentacao(
            @PathVariable id: UUID,
            @Valid @RequestBody request: RegistrarMovimentacaoAnimalRequest
    ): ResponseEntity<MovimentacaoAnimalDto> {
        planoAcessoService.requireRecurso(
                SecurityUtils.currentEmpresaId(),
                PlanoRecurso.MOVIMENTACAO,
                "Movimentacao de animais faz parte do plano Basic ou superior."
        )
        val movimentacao = registrarMovimentacaoAnimalUseCase.execute(
                SecurityUtils.currentFarmId(),
                SecurityUtils.currentEmail(),
                id,
                request
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(movimentacao)
    }

    @PostMapping("/movimentacoes-em-lote")
    @Operation(summary = "Registrar movimentacao em lote", description = "Aplica movimentacao em massa para varios animais do lote com fluxo otimizado para campo")
    fun registrarMovimentacaoEmLote(
            @Valid @RequestBody request: RegistrarMovimentacaoLoteRequest
    ): ResponseEntity<MovimentacaoLoteResultadoDto> {
        planoAcessoService.requireRecurso(
                SecurityUtils.currentEmpresaId(),
                PlanoRecurso.MOVIMENTACAO,
                "Movimentacao de animais faz parte do plano Basic ou superior."
        )
        val resultado = registrarMovimentacaoLoteUseCase.execute(
                SecurityUtils.currentFarmId(),
                SecurityUtils.currentEmail(),
                request
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(resultado)
    }

    @GetMapping("/{id}/movimentacoes")
    @Operation(summary = "Listar movimentacoes do animal", description = "Retorna a trilha de rastreabilidade do animal")
    fun listarMovimentacoes(@PathVariable id: UUID): ResponseEntity<List<MovimentacaoAnimalDto>> {
        return ResponseEntity.ok(listarMovimentacoesAnimalUseCase.execute(id, SecurityUtils.currentFarmId()))
    }

    @GetMapping("/{id}/pesagens")
    @Operation(summary = "Listar pesagens do animal", description = "Retorna o historico estruturado de pesagens do animal em ordem cronologica reversa")
    fun listarPesagens(@PathVariable id: UUID): ResponseEntity<List<PesagemAnimalDto>> {
        return ResponseEntity.ok(listarPesagensAnimalUseCase.execute(id, SecurityUtils.currentFarmId()))
    }

    @PostMapping("/{id}/vacinacoes")
    @Operation(summary = "Registrar vacinacao", description = "Registra vacinacao estruturada do animal")
    fun registrarVacinacao(
            @PathVariable id: UUID,
            @Valid @RequestBody request: RegistrarVacinacaoAnimalRequest
    ): ResponseEntity<VacinacaoAnimalDto> {
        planoAcessoService.requireRecurso(
                SecurityUtils.currentEmpresaId(),
                PlanoRecurso.VACINACAO,
                "Vacinacao estruturada faz parte do plano Basic ou superior."
        )
        val vacinacao = registrarVacinacaoAnimalUseCase.execute(
                SecurityUtils.currentFarmId(),
                SecurityUtils.currentEmail(),
                id,
                request
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(vacinacao)
    }

    @GetMapping("/{id}/vacinacoes")
    @Operation(summary = "Listar vacinacoes", description = "Retorna o historico de vacinacao do animal")
    fun listarVacinacoes(@PathVariable id: UUID): ResponseEntity<List<VacinacaoAnimalDto>> {
        return ResponseEntity.ok(listarVacinacoesAnimalUseCase.execute(id, SecurityUtils.currentFarmId()))
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar animal", description = "Remove um animal do rebanho (soft delete)")
    fun deletar(@PathVariable id: UUID): ResponseEntity<Void> {
        deletarAnimalUseCase.execute(id, SecurityUtils.currentFarmId())
        return ResponseEntity.noContent().build()
    }
}
