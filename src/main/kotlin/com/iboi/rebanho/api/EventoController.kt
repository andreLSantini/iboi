package com.iboi.rebanho.api

import com.iboi.plano.model.PlanoRecurso
import com.iboi.plano.service.PlanoAcessoService
import com.iboi.rebanho.api.dto.EventoDto
import com.iboi.rebanho.api.dto.RegistrarEventoRequest
import com.iboi.rebanho.api.dto.toDto
import com.iboi.rebanho.domain.TipoEvento
import com.iboi.rebanho.repository.EventoRepository
import com.iboi.rebanho.usecase.RegistrarEventoUseCase
import com.iboi.shared.security.SecurityUtils
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.util.UUID

@RestController
@RequestMapping("/api/eventos")
@Tag(name = "Eventos", description = "Registro de eventos do rebanho")
class EventoController(
        private val planoAcessoService: PlanoAcessoService,
        private val eventoRepository: EventoRepository,
        private val registrarEventoUseCase: RegistrarEventoUseCase
) {

    @PostMapping
    @Operation(summary = "Registrar evento", description = "Registra um novo evento para um animal")
    @ApiResponses(
            value = [
                ApiResponse(responseCode = "201", description = "Evento registrado com sucesso"),
                ApiResponse(responseCode = "400", description = "Dados invalidos"),
                ApiResponse(responseCode = "404", description = "Animal nao encontrado")
            ]
    )
    fun registrar(@Valid @RequestBody request: RegistrarEventoRequest): ResponseEntity<EventoDto> {
        planoAcessoService.requireRecurso(
                SecurityUtils.currentEmpresaId(),
                when (request.tipo) {
                    TipoEvento.PESAGEM -> PlanoRecurso.PESAGEM
                    TipoEvento.VACINA -> PlanoRecurso.VACINACAO
                    else -> PlanoRecurso.CADASTRO_COMPLETO
                },
                "O registro operacional deste evento faz parte do plano Basic ou superior."
        )
        val evento = registrarEventoUseCase.execute(
                SecurityUtils.currentFarmId(),
                SecurityUtils.currentEmail(),
                request
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(evento)
    }

    @GetMapping
    fun listar(
            @RequestParam(required = false) tipo: TipoEvento?,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) dataInicio: LocalDate?,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) dataFim: LocalDate?,
            @RequestParam(required = false) animalId: UUID?
    ): ResponseEntity<List<EventoDto>> {
        val farmId = SecurityUtils.currentFarmId()

        val eventos = when {
            animalId != null -> eventoRepository.findByAnimalIdOrderByDataDesc(animalId)
            tipo != null -> eventoRepository.findByFarmIdAndTipoOrderByDataDesc(farmId, tipo)
            dataInicio != null && dataFim != null -> eventoRepository.findByFarmIdAndDataBetween(farmId, dataInicio, dataFim)
            else -> eventoRepository.findByFarmIdOrderByDataDesc(farmId)
        }

        return ResponseEntity.ok(eventos.map { it.toDto() })
    }

    @GetMapping("/animal/{animalId}")
    fun listarPorAnimal(@PathVariable animalId: UUID): ResponseEntity<List<EventoDto>> {
        return ResponseEntity.ok(eventoRepository.findByAnimalIdOrderByDataDesc(animalId).map { it.toDto() })
    }

    @GetMapping("/{id}")
    fun buscarPorId(@PathVariable id: UUID): ResponseEntity<EventoDto> {
        val farmId = SecurityUtils.currentFarmId()
        val evento = eventoRepository.findById(id).orElse(null) ?: return ResponseEntity.notFound().build()

        if (evento.farm.id != farmId) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }

        return ResponseEntity.ok(evento.toDto())
    }
}
