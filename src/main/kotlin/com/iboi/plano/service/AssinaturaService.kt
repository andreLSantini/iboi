package com.iboi.plano.service

import com.iboi.plano.model.Assinatura
import com.iboi.plano.model.PeriodoPagamento
import com.iboi.plano.model.StatusAssinatura
import com.iboi.plano.model.TipoAssinatura
import com.iboi.plano.repository.AssinaturaRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*

@Service
class AssinaturaService(
        private val assinaturaRepository: AssinaturaRepository
) {

    fun isAssinaturaAtiva(empresaId: UUID): Boolean {
        val assinatura = assinaturaRepository.findByEmpresaId(empresaId)
                ?: return false
        verificarEAtualizarStatus(assinatura)

        if (assinatura.tipo == TipoAssinatura.FREE) {
            return assinatura.status == StatusAssinatura.ATIVA
        }

        return when (assinatura.status) {
            StatusAssinatura.TRIAL -> {
                // Verificar se o trial ainda está válido
                LocalDateTime.now().isBefore(assinatura.dataVencimento)
            }
            StatusAssinatura.ATIVA -> {
                // Verificar se a assinatura ainda está válida
                LocalDateTime.now().isBefore(assinatura.dataVencimento)
            }
            StatusAssinatura.VENCIDA,
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
