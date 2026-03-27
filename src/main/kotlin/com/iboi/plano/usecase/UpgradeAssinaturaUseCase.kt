package com.iboi.plano.usecase

import com.iboi.plano.api.dto.UpgradeRequest
import com.iboi.plano.model.PlanoPreco
import com.iboi.plano.model.StatusAssinatura
import com.iboi.plano.model.TipoAssinatura
import com.iboi.plano.repository.AssinaturaRepository
import com.iboi.plano.service.AssinaturaService
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Component
class UpgradeAssinaturaUseCase(
        private val assinaturaRepository: AssinaturaRepository,
        private val assinaturaService: AssinaturaService
) {

    @Transactional
    fun execute(empresaId: UUID, request: UpgradeRequest) {
        val assinatura = assinaturaRepository.findByEmpresaId(empresaId)
                ?: throw IllegalStateException("Assinatura nao encontrada")

        if (request.novoPlano == TipoAssinatura.TRIAL || request.novoPlano == TipoAssinatura.FREE) {
            throw IllegalArgumentException("Nao e possivel fazer upgrade para o plano de entrada")
        }

        if (assinatura.tipo == request.novoPlano && assinatura.status == StatusAssinatura.ATIVA) {
            throw IllegalArgumentException("Empresa ja possui este plano ativo")
        }

        val valor = PlanoPreco.getValor(request.novoPlano, request.periodo)
        val agora = LocalDateTime.now()
        val proximaCobranca = assinaturaService.calcularProximaCobranca(agora, request.periodo)

        assinatura.tipo = request.novoPlano
        assinatura.status = StatusAssinatura.VENCIDA
        assinatura.periodoPagamento = request.periodo
        assinatura.dataVencimento = agora.plusDays(7)
        assinatura.proximaCobranca = proximaCobranca
        assinatura.valor = valor

        assinaturaRepository.save(assinatura)
    }
}
