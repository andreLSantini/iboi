package com.iboi.plano.usecase

import com.iboi.plano.model.StatusAssinatura
import com.iboi.plano.repository.AssinaturaRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Component
class CancelarAssinaturaUseCase(
        private val assinaturaRepository: AssinaturaRepository
) {

    @Transactional
    fun execute(empresaId: UUID, motivo: String?) {
        val assinatura = assinaturaRepository.findByEmpresaId(empresaId)
                ?: throw IllegalStateException("Assinatura não encontrada")

        if (assinatura.status == StatusAssinatura.CANCELADA) {
            throw IllegalArgumentException("Assinatura já está cancelada")
        }

        assinatura.status = StatusAssinatura.CANCELADA
        assinaturaRepository.save(assinatura)

        // TODO: Registrar motivo do cancelamento em log ou tabela separada se necessário
    }
}
