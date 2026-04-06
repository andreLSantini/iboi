package com.iboi.agenda.api

import com.iboi.agenda.api.dto.AgendaOperacionalResponse
import com.iboi.agenda.usecase.AgendaOperacionalUseCase
import com.iboi.shared.security.SecurityUtils
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/agenda")
@Tag(name = "Agenda Operacional", description = "Agenda unificada de manejo da fazenda")
class AgendaOperacionalController(
        private val agendaOperacionalUseCase: AgendaOperacionalUseCase
) {

    @GetMapping("/operacional")
    @Operation(summary = "Obter agenda operacional", description = "Consolida agenda sanitaria, reprodutiva e pendencias de pesagem")
    fun listar(): ResponseEntity<AgendaOperacionalResponse> {
        return ResponseEntity.ok(agendaOperacionalUseCase.execute(SecurityUtils.currentFarmId()))
    }
}
