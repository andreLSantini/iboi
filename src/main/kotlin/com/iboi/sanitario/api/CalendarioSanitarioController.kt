package com.iboi.sanitario.api

import com.iboi.sanitario.api.dto.CalendarioSanitarioResponse
import com.iboi.sanitario.usecase.CalendarioSanitarioUseCase
import com.iboi.shared.security.SecurityUtils
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/sanitario")
@Tag(name = "Calendario Sanitario", description = "Gestao de protocolos e agendamentos de vacinas e vermifugos")
class CalendarioSanitarioController(
        private val calendarioSanitarioUseCase: CalendarioSanitarioUseCase
) {

    @GetMapping("/calendario")
    @Operation(summary = "Obter calendario sanitario", description = "Retorna agendamentos pendentes, atrasados e proximos 30 dias")
    fun getCalendario(): ResponseEntity<CalendarioSanitarioResponse> {
        return ResponseEntity.ok(calendarioSanitarioUseCase.execute(SecurityUtils.currentFarmId()))
    }
}
