package com.iboi.plano.service

import com.iboi.plano.model.Assinatura
import com.iboi.plano.model.PeriodoPagamento
import com.iboi.plano.model.StatusAssinatura
import com.iboi.plano.model.TipoAssinatura
import com.iboi.plano.repository.AssinaturaRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.UUID

@Service
class AssinaturaService(
        private val assinaturaRepository: AssinaturaRepository
) {

    fun isAssinaturaAtiva(empresaId: UUID): Boolean {
        val assinatura = assinaturaRepository.findByEmpresaId(empresaId)
                ?: return false
        verificarEAtualizarStatus(assinatura)
        val agora = LocalDateTime.now()

        if (assinatura.tipo == TipoAssinatura.FREE) {
            return assinatura.status == StatusAssinatura.ATIVA
        }

        return when (assinatura.status) {
            StatusAssinatura.TRIAL -> agora.isBefore(assinatura.dataVencimento)
            StatusAssinatura.ATIVA -> agora.isBefore(assinatura.dataVencimento)
            StatusAssinatura.VENCIDA -> agora.isBefore(assinatura.dataVencimento)
            StatusAssinatura.CANCELADA,
            StatusAssinatura.SUSPENSA -> false
        }
    }

    fun verificarEAtualizarStatus(assinatura: Assinatura) {
        val agora = LocalDateTime.now()

        if (assinatura.tipo == TipoAssinatura.FREE) {
            if (assinatura.status != StatusAssinatura.CANCELADA && assinatura.status != StatusAssinatura.SUSPENSA) {
                assinatura.status = StatusAssinatura.ATIVA
            }
            return
        }

        if (agora.isAfter(assinatura.dataVencimento)) {
            when (assinatura.status) {
                StatusAssinatura.TRIAL -> {
                    assinatura.status = StatusAssinatura.VENCIDA
                    assinaturaRepository.save(assinatura)
                }
                StatusAssinatura.ATIVA -> {
                    assinatura.status = StatusAssinatura.VENCIDA
                    assinaturaRepository.save(assinatura)
                }
                else -> {}
            }
        }
    }

    fun calcularProximaCobranca(
            dataAtual: LocalDateTime,
            periodo: PeriodoPagamento
    ): LocalDateTime {
        return dataAtual.plusMonths(periodo.meses.toLong())
    }

    fun getAssinatura(empresaId: UUID): Assinatura? {
        return assinaturaRepository.findByEmpresaId(empresaId)
    }
}
