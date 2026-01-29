package com.iboi.plano.api

import com.iboi.plano.api.dto.AssinaturaDto
import com.iboi.plano.api.dto.CancelarAssinaturaRequest
import com.iboi.plano.api.dto.UpgradeRequest
import com.iboi.identity.infrastructure.repository.UsuarioRepository
import com.iboi.plano.repository.AssinaturaRepository
import com.iboi.plano.usecase.CancelarAssinaturaUseCase
import com.iboi.plano.usecase.UpgradeAssinaturaUseCase
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import java.time.Duration
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/assinatura")
class AssinaturaController(
        private val assinaturaRepository: AssinaturaRepository,
        private val usuarioRepository: UsuarioRepository,
        private val upgradeAssinaturaUseCase: UpgradeAssinaturaUseCase,
        private val cancelarAssinaturaUseCase: CancelarAssinaturaUseCase
) {

    @GetMapping("/minha")
    fun getMinhaAssinatura(): ResponseEntity<AssinaturaDto> {
        val empresaId = getEmpresaIdFromAuth()
        val assinatura = assinaturaRepository.findByEmpresaId(empresaId)
                ?: return ResponseEntity.notFound().build()

        val agora = LocalDateTime.now()
        val diasRestantes = Duration.between(agora, assinatura.dataVencimento).toDays()

        val dto = AssinaturaDto(
                id = assinatura.id!!,
                tipo = assinatura.tipo,
                status = assinatura.status,
                periodoPagamento = assinatura.periodoPagamento,
                dataInicio = assinatura.dataInicio,
                dataVencimento = assinatura.dataVencimento,
                proximaCobranca = assinatura.proximaCobranca,
                valor = assinatura.valor,
                diasRestantes = diasRestantes
        )

        return ResponseEntity.ok(dto)
    }

    @PostMapping("/upgrade")
    fun upgrade(@RequestBody request: UpgradeRequest): ResponseEntity<Map<String, String>> {
        val empresaId = getEmpresaIdFromAuth()
        upgradeAssinaturaUseCase.execute(empresaId, request)

        return ResponseEntity.ok(
                mapOf("mensagem" to "Upgrade realizado com sucesso! Realize o pagamento para ativar o plano.")
        )
    }

    @PostMapping("/cancelar")
    fun cancelar(@RequestBody request: CancelarAssinaturaRequest): ResponseEntity<Map<String, String>> {
        val empresaId = getEmpresaIdFromAuth()
        cancelarAssinaturaUseCase.execute(empresaId, request.motivo)

        return ResponseEntity.ok(
                mapOf("mensagem" to "Assinatura cancelada com sucesso.")
        )
    }

    private fun getEmpresaIdFromAuth(): java.util.UUID {
        val email = SecurityContextHolder.getContext().authentication.principal as String
        val usuario = usuarioRepository.findByEmail(email)
                ?: throw IllegalStateException("Usuário não encontrado")
        return usuario.empresa.id!!
    }
}
