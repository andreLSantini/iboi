package com.iboi.ia.api

import com.iboi.ia.api.dto.AlertaDto
import com.iboi.ia.domain.StatusAlerta
import com.iboi.ia.repository.AlertaRepository
import com.iboi.ia.usecase.GerarAlertasUseCase
import com.iboi.identity.infrastructure.repository.UsuarioRepository
import com.iboi.rebanho.api.dto.AnimalResumoDto
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import java.util.*

@RestController
@RequestMapping("/api/alertas")
@Tag(name = "Alertas IA", description = "Sistema inteligente de alertas e recomendações")
class AlertaController(
        private val alertaRepository: AlertaRepository,
        private val gerarAlertasUseCase: GerarAlertasUseCase,
        private val usuarioRepository: UsuarioRepository
) {

    @GetMapping
    @Operation(summary = "Listar todos os alertas")
    fun listar(): ResponseEntity<List<AlertaDto>> {
        val farmId = getFarmIdFromAuth()
        val alertas = alertaRepository.findByFarmIdOrderByCriadoEmDesc(farmId)
        return ResponseEntity.ok(alertas.map { toDto(it) })
    }

    @GetMapping("/ativos")
    @Operation(summary = "Listar alertas ativos", description = "Retorna apenas alertas não lidos ou não resolvidos")
    fun ativos(): ResponseEntity<List<AlertaDto>> {
        val farmId = getFarmIdFromAuth()
        val alertas = alertaRepository.findByFarmIdAndStatusOrderByPrioridadeDescCriadoEmDesc(farmId, StatusAlerta.ATIVO)
        return ResponseEntity.ok(alertas.map { toDto(it) })
    }

    @PostMapping("/{id}/marcar-lido")
    @Operation(summary = "Marcar alerta como lido")
    fun marcarLido(@PathVariable id: UUID): ResponseEntity<Void> {
        val alerta = alertaRepository.findById(id).orElse(null) ?: return ResponseEntity.notFound().build()
        alerta.status = StatusAlerta.LIDO
        alerta.lidoEm = LocalDateTime.now()
        alertaRepository.save(alerta)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/{id}/resolver")
    @Operation(summary = "Marcar alerta como resolvido")
    fun resolver(@PathVariable id: UUID): ResponseEntity<Void> {
        val alerta = alertaRepository.findById(id).orElse(null) ?: return ResponseEntity.notFound().build()
        alerta.status = StatusAlerta.RESOLVIDO
        alerta.resolvidoEm = LocalDateTime.now()
        alertaRepository.save(alerta)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/gerar")
    @Operation(summary = "Gerar alertas (admin)", description = "Executa detectores de alertas e cria novos alertas")
    fun gerar(): ResponseEntity<Map<String, Int>> {
        val farmId = getFarmIdFromAuth()
        val quantidade = gerarAlertasUseCase.execute(farmId)
        return ResponseEntity.ok(mapOf("alertasGerados" to quantidade))
    }

    private fun getFarmIdFromAuth(): UUID {
        val email = SecurityContextHolder.getContext().authentication.principal as String
        val usuario = usuarioRepository.findByEmail(email)
                ?: throw IllegalStateException("Usuário não encontrado")
        return usuario.empresa.id!!
    }

    private fun toDto(alerta: com.iboi.ia.domain.Alerta): AlertaDto {
        return AlertaDto(
                id = alerta.id!!,
                tipo = alerta.tipo,
                prioridade = alerta.prioridade,
                titulo = alerta.titulo,
                mensagem = alerta.mensagem,
                animal = alerta.animal?.let {
                    AnimalResumoDto(it.id!!, it.brinco, it.nome)
                },
                recomendacao = alerta.recomendacao,
                status = alerta.status,
                criadoEm = alerta.criadoEm
        )
    }
}
