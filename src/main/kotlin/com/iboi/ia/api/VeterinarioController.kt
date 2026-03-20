package com.iboi.ia.api

import com.iboi.ia.domain.CompartilhamentoVeterinario
import com.iboi.ia.repository.CompartilhamentoVeterinarioRepository
import com.iboi.identity.infrastructure.repository.FarmRepository
import com.iboi.shared.security.SecurityUtils
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import java.util.UUID

@RestController
@RequestMapping("/api/veterinarios")
@Tag(name = "Veterinarios", description = "Compartilhamento de acesso com veterinarios")
class VeterinarioController(
        private val compartilhamentoRepository: CompartilhamentoVeterinarioRepository,
        private val farmRepository: FarmRepository
) {

    @PostMapping("/convidar")
    @Operation(summary = "Convidar veterinario", description = "Compartilha acesso da fazenda com um veterinario")
    fun convidar(@RequestBody request: ConvidarVeterinarioRequest): ResponseEntity<ConvidarVeterinarioResponse> {
        val farm = farmRepository.findById(SecurityUtils.currentFarmId()).orElseThrow()
        val token = UUID.randomUUID().toString()

        val compartilhamento = compartilhamentoRepository.save(
                CompartilhamentoVeterinario(
                        farm = farm,
                        nomeVeterinario = request.nome,
                        emailVeterinario = request.email,
                        crmv = request.crmv,
                        tokenAcesso = token,
                        dataExpiracao = LocalDateTime.now().plusDays(365)
                )
        )

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ConvidarVeterinarioResponse(
                        id = compartilhamento.id!!,
                        token = token,
                        mensagem = "Convite enviado para ${request.email}"
                )
        )
    }

    @GetMapping
    @Operation(summary = "Listar veterinarios com acesso")
    fun listar(): ResponseEntity<List<VeterinarioDto>> {
        val compartilhamentos = compartilhamentoRepository.findByFarmIdAndAtivo(
                SecurityUtils.currentFarmId(),
                true
        )

        val dtos = compartilhamentos.map {
            VeterinarioDto(
                    id = it.id!!,
                    nome = it.nomeVeterinario,
                    email = it.emailVeterinario,
                    crmv = it.crmv,
                    dataConvite = it.criadoEm
            )
        }

        return ResponseEntity.ok(dtos)
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Revogar acesso")
    fun revogar(@PathVariable id: UUID): ResponseEntity<Void> {
        val compartilhamento = compartilhamentoRepository.findById(id).orElse(null)
                ?: return ResponseEntity.notFound().build()

        compartilhamento.ativo = false
        compartilhamentoRepository.save(compartilhamento)

        return ResponseEntity.noContent().build()
    }
}

data class ConvidarVeterinarioRequest(
        val nome: String,
        val email: String,
        val crmv: String?
)

data class ConvidarVeterinarioResponse(
        val id: UUID,
        val token: String,
        val mensagem: String
)

data class VeterinarioDto(
        val id: UUID,
        val nome: String,
        val email: String,
        val crmv: String?,
        val dataConvite: LocalDateTime
)
