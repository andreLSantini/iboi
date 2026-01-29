package com.iboi.sanitario.api

import com.iboi.identity.infrastructure.repository.UsuarioRepository
import com.iboi.sanitario.api.dto.CalendarioSanitarioResponse
import com.iboi.sanitario.usecase.CalendarioSanitarioUseCase
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/sanitario")
@Tag(name = "Calendário Sanitário", description = "Gestão de protocolos e agendamentos de vacinas/vermífugos")
class CalendarioSanitarioController(
        private val calendarioSanitarioUseCase: CalendarioSanitarioUseCase,
        private val usuarioRepository: UsuarioRepository
) {

    @GetMapping("/calendario")
    @Operation(summary = "Obter calendário sanitário", description = "Retorna agendamentos pendentes, atrasados e próximos 30 dias")
    fun getCalendario(): ResponseEntity<CalendarioSanitarioResponse> {
        val farmId = getFarmIdFromAuth()
        val calendario = calendarioSanitarioUseCase.execute(farmId)
        return ResponseEntity.ok(calendario)
    }

    private fun getFarmIdFromAuth(): UUID {
        val email = SecurityContextHolder.getContext().authentication.principal as String
        val usuario = usuarioRepository.findByEmail(email)
                ?: throw IllegalStateException("Usuário não encontrado")
        return usuario.empresa.id!!
    }
}
