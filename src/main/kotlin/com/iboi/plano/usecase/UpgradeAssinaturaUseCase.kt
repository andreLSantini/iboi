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
import java.util.*

@Component
class UpgradeAssinaturaUseCase(
        private val assinaturaRepository: AssinaturaRepository,
        private val assinaturaService: AssinaturaService
) {

    @Transactional
    fun execute(empresaId: UUID, request: UpgradeRequest) {
        val assinatura = assinaturaRepository.findByEmpresaId(empresaId)
                ?: throw IllegalStateException("Assinatura não encontrada")

        // Validações
        if (request.novoPlano == TipoAssinatura.TRIAL) {
            throw IllegalArgumentException("Não é possível fazer upgrade para plano TRIAL")
        }

        if (assinatura.tipo == request.novoPlano && assinatura.status == StatusAssinatura.ATIVA) {
            throw IllegalArgumentException("Empresa já possui este plano ativo")
        }

        // Calcular valor
        val valor = PlanoPreco.getValor(request.novoPlano, request.periodo)

        // Calcular próxima cobrança
        val agora = LocalDateTime.now()
        val proximaCobranca = assinaturaService.calcularProximaCobranca(agora, request.periodo)

        // Atualizar assinatura
        assinatura.tipo = request.novoPlano
        assinatura.status = StatusAssinatura.VENCIDA // Aguardando pagamento
        assinatura.periodoPagamento = request.periodo
        assinatura.dataVencimento = agora.plusDays(7) // 7 dias para pagar
        assinatura.proximaCobranca = proximaCobranca
        assinatura.valor = valor

        assinaturaRepository.save(assinatura)
    }
}
