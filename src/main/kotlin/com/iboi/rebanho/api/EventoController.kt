package com.iboi.rebanho.api

import com.iboi.identity.infrastructure.repository.UsuarioRepository
import com.iboi.identity.infrastructure.repository.UserFarmProfileRepository
import com.iboi.rebanho.api.dto.*
import com.iboi.rebanho.domain.TipoEvento
import com.iboi.rebanho.repository.EventoRepository
import com.iboi.rebanho.usecase.RegistrarEventoUseCase
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.util.*

@RestController
@RequestMapping("/api/eventos")
class EventoController(
        private val eventoRepository: EventoRepository,
        private val usuarioRepository: UsuarioRepository,
        private val userFarmProfileRepository: UserFarmProfileRepository,
        private val registrarEventoUseCase: RegistrarEventoUseCase
) {

    @PostMapping
    fun registrar(@RequestBody request: RegistrarEventoRequest): ResponseEntity<EventoDto> {
        val farmId = getFarmIdFromAuth()
        val email = getEmailFromAuth()
        val evento = registrarEventoUseCase.execute(farmId, email, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(evento)
    }

    @GetMapping
    fun listar(
            @RequestParam(required = false) tipo: TipoEvento?,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) dataInicio: LocalDate?,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) dataFim: LocalDate?,
            @RequestParam(required = false) animalId: UUID?
    ): ResponseEntity<List<EventoDto>> {
        val farmId = getFarmIdFromAuth()

        val eventos = when {
            animalId != null -> eventoRepository.findByAnimalIdOrderByDataDesc(animalId)
            tipo != null -> eventoRepository.findByFarmIdAndTipoOrderByDataDesc(farmId, tipo)
            dataInicio != null && dataFim != null -> eventoRepository.findByFarmIdAndDataBetween(farmId, dataInicio, dataFim)
            else -> eventoRepository.findByFarmIdOrderByDataDesc(farmId)
        }

        val dtos = eventos.map { toDto(it) }
        return ResponseEntity.ok(dtos)
    }

    @GetMapping("/animal/{animalId}")
    fun listarPorAnimal(@PathVariable animalId: UUID): ResponseEntity<List<EventoDto>> {
        val eventos = eventoRepository.findByAnimalIdOrderByDataDesc(animalId)
        val dtos = eventos.map { toDto(it) }
        return ResponseEntity.ok(dtos)
    }

    @GetMapping("/{id}")
    fun buscarPorId(@PathVariable id: UUID): ResponseEntity<EventoDto> {
        val farmId = getFarmIdFromAuth()
        val evento = eventoRepository.findById(id).orElse(null) ?: return ResponseEntity.notFound().build()

        if (evento.farm.id != farmId) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }

        return ResponseEntity.ok(toDto(evento))
    }

    private fun getFarmIdFromAuth(): UUID {
        val email = getEmailFromAuth()
        val usuario = usuarioRepository.findByEmail(email)
                ?: throw IllegalStateException("Usuário não encontrado")

        // Buscar o perfil default do usuário
        val userFarmProfile = userFarmProfileRepository.findByUsuario_IdAndIsDefaultTrue(usuario.id!!)
                ?: userFarmProfileRepository.findByUsuario_Id(usuario.id!!)
                ?: throw IllegalStateException("Usuário não possui fazenda associada")

        return userFarmProfile.farm.id!!
    }

    private fun getEmailFromAuth(): String {
        return SecurityContextHolder.getContext().authentication.principal as String
    }

    private fun toDto(evento: com.iboi.rebanho.domain.Evento): EventoDto {
        return EventoDto(
                id = evento.id!!,
                animal = AnimalResumoDto(
                        id = evento.animal.id!!,
                        brinco = evento.animal.brinco,
                        nome = evento.animal.nome
                ),
                tipo = evento.tipo,
                data = evento.data,
                descricao = evento.descricao,
                peso = evento.peso,
                produto = evento.produto,
                dose = evento.dose,
                unidadeMedida = evento.unidadeMedida,
                loteDestino = evento.loteDestino?.let {
                    LoteResumoDto(it.id!!, it.nome)
                },
                valor = evento.valor,
                responsavel = evento.responsavel?.nome
        )
    }
}
