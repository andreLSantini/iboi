package com.iboi.ia.api

import com.iboi.identity.infrastructure.repository.FarmRepository
import com.iboi.identity.infrastructure.repository.UsuarioRepository
import com.iboi.ia.domain.CompartilhamentoVeterinario
import com.iboi.ia.repository.CompartilhamentoVeterinarioRepository
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import java.util.*

@RestController
@RequestMapping("/api/veterinarios")
@Tag(name = "Veterinários", description = "Compartilhamento de acesso com veterinários")
class VeterinarioController(
        private val compartilhamentoRepository: CompartilhamentoVeterinarioRepository,
        private val farmRepository: FarmRepository,
        private val usuarioRepository: UsuarioRepository
) {

    @PostMapping("/convidar")
    @Operation(summary = "Convidar veterinário", description = "Compartilha acesso da fazenda com um veterinário")
    fun convidar(@RequestBody request: ConvidarVeterinarioRequest): ResponseEntity<ConvidarVeterinarioResponse> {
        val farmId = getFarmIdFromAuth()
        val farm = farmRepository.findById(farmId).orElseThrow()

        val token = UUID.randomUUID().toString()

        val compartilhamento = compartilhamentoRepository.save(
                CompartilhamentoVeterinario(
                        farm = farm,
                        nomeVeterinario = request.nome,
                        emailVeterinario = request.email,
                        crmv = request.crmv,
                        tokenAcesso = token,
                        dataExpiracao = LocalDateTime.now().plusDays(365) // 1 ano
                )
        )

        // TODO: Enviar email com convite

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ConvidarVeterinarioResponse(
                        id = compartilhamento.id!!,
                        token = token,
                        mensagem = "Convite enviado para ${request.email}"
                )
        )
    }

    @GetMapping
    @Operation(summary = "Listar veterinários com acesso")
    fun listar(): ResponseEntity<List<VeterinarioDto>> {
        val farmId = getFarmIdFromAuth()
        val compartilhamentos = compartilhamentoRepository.findByFarmIdAndAtivo(farmId, true)

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

    private fun getFarmIdFromAuth(): UUID {
        val email = SecurityContextHolder.getContext().authentication.principal as String
        val usuario = usuarioRepository.findByEmail(email)
                ?: throw IllegalStateException("Usuário não encontrado")
        return usuario.empresa.id!!
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
