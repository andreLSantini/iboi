package com.iboi.plano.api

import com.iboi.plano.api.dto.AssinaturaDto
import com.iboi.plano.api.dto.CancelarAssinaturaRequest
import com.iboi.plano.api.dto.UpgradeRequest
import com.iboi.plano.repository.AssinaturaRepository
import com.iboi.plano.service.AssinaturaService
import com.iboi.plano.service.PlanoAcessoService
import com.iboi.plano.usecase.CancelarAssinaturaUseCase
import com.iboi.plano.usecase.UpgradeAssinaturaUseCase
import com.iboi.shared.security.SecurityUtils
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Duration
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/assinatura")
class AssinaturaController(
        private val assinaturaRepository: AssinaturaRepository,
        private val assinaturaService: AssinaturaService,
        private val planoAcessoService: PlanoAcessoService,
        private val upgradeAssinaturaUseCase: UpgradeAssinaturaUseCase,
        private val cancelarAssinaturaUseCase: CancelarAssinaturaUseCase
) {

    @GetMapping("/minha")
    fun getMinhaAssinatura(): ResponseEntity<AssinaturaDto> {
        val assinatura = assinaturaRepository.findByEmpresaId(SecurityUtils.currentEmpresaId())
                ?: return ResponseEntity.notFound().build()

        assinaturaService.verificarEAtualizarStatus(assinatura)
        val resumoPlano = planoAcessoService.resumo(SecurityUtils.currentEmpresaId())

        val agora = LocalDateTime.now()
        val diasRestantes = maxOf(0, Duration.between(agora, assinatura.dataVencimento).toDays())

        val dto = AssinaturaDto(
                id = assinatura.id!!,
                tipo = assinatura.tipo,
                status = assinatura.status,
                periodoPagamento = assinatura.periodoPagamento,
                dataInicio = assinatura.dataInicio,
                dataVencimento = assinatura.dataVencimento,
                proximaCobranca = assinatura.proximaCobranca,
                valor = assinatura.valor,
                diasRestantes = diasRestantes,
                tituloPlano = resumoPlano.titulo,
                descricaoPlano = resumoPlano.descricao,
                limiteAnimais = resumoPlano.limiteAnimais,
                animaisCadastrados = resumoPlano.animaisCadastrados,
                recursos = resumoPlano.recursos.toList()
        )

        return ResponseEntity.ok(dto)
    }

    @PostMapping("/upgrade")
    fun upgrade(@RequestBody request: UpgradeRequest): ResponseEntity<Map<String, String>> {
        upgradeAssinaturaUseCase.execute(SecurityUtils.currentEmpresaId(), request)
        return ResponseEntity.ok(
                mapOf("mensagem" to "Upgrade realizado com sucesso! Realize o pagamento para ativar o plano.")
        )
    }

    @PostMapping("/cancelar")
    fun cancelar(@RequestBody request: CancelarAssinaturaRequest): ResponseEntity<Map<String, String>> {
        cancelarAssinaturaUseCase.execute(SecurityUtils.currentEmpresaId(), request.motivo)
        return ResponseEntity.ok(mapOf("mensagem" to "Assinatura cancelada com sucesso."))
    }
}
