package com.iboi.ia.api

import com.iboi.ia.api.dto.AlertaDto
import com.iboi.ia.domain.StatusAlerta
import com.iboi.ia.repository.AlertaRepository
import com.iboi.ia.usecase.GerarAlertasUseCase
import com.iboi.rebanho.api.dto.AnimalResumoDto
import com.iboi.shared.security.SecurityUtils
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import java.util.UUID

@RestController
@RequestMapping("/api/alertas")
@Tag(name = "Alertas IA", description = "Sistema inteligente de alertas e recomendacoes")
class AlertaController(
        private val alertaRepository: AlertaRepository,
        private val gerarAlertasUseCase: GerarAlertasUseCase
) {

    @GetMapping
    @Operation(summary = "Listar todos os alertas")
    fun listar(): ResponseEntity<List<AlertaDto>> {
        val alertas = alertaRepository.findByFarmIdOrderByCriadoEmDesc(SecurityUtils.currentFarmId())
        return ResponseEntity.ok(alertas.map { toDto(it) })
    }

    @GetMapping("/ativos")
    @Operation(summary = "Listar alertas ativos", description = "Retorna apenas alertas nao lidos ou nao resolvidos")
    fun ativos(): ResponseEntity<List<AlertaDto>> {
        val alertas = alertaRepository.findByFarmIdAndStatusOrderByPrioridadeDescCriadoEmDesc(
                SecurityUtils.currentFarmId(),
                StatusAlerta.ATIVO
        )
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
        val quantidade = gerarAlertasUseCase.execute(SecurityUtils.currentFarmId())
        return ResponseEntity.ok(mapOf("alertasGerados" to quantidade))
    }

    private fun toDto(alerta: com.iboi.ia.domain.Alerta): AlertaDto {
        return AlertaDto(
                id = alerta.id!!,
                tipo = alerta.tipo,
                prioridade = alerta.prioridade,
                titulo = alerta.titulo,
                mensagem = alerta.mensagem,
                animal = alerta.animal?.let { AnimalResumoDto(it.id!!, it.brinco, it.nome) },
                recomendacao = alerta.recomendacao,
                status = alerta.status,
                criadoEm = alerta.criadoEm
        )
    }
}
