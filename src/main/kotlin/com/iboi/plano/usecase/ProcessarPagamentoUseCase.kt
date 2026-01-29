package com.iboi.plano.usecase

import com.iboi.plano.api.dto.PagamentoDto
import com.iboi.plano.api.dto.ProcessarPagamentoRequest
import com.iboi.plano.api.dto.ProcessarPagamentoResponse
import com.iboi.plano.model.Pagamento
import com.iboi.plano.model.StatusAssinatura
import com.iboi.plano.model.StatusPagamento
import com.iboi.plano.repository.AssinaturaRepository
import com.iboi.plano.repository.PagamentoRepository
import com.iboi.plano.service.AssinaturaService
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Component
class ProcessarPagamentoUseCase(
        private val assinaturaRepository: AssinaturaRepository,
        private val pagamentoRepository: PagamentoRepository,
        private val assinaturaService: AssinaturaService
) {

    @Transactional
    fun execute(empresaId: UUID, request: ProcessarPagamentoRequest): ProcessarPagamentoResponse {
        val assinatura = assinaturaRepository.findByEmpresaId(empresaId)
                ?: throw IllegalStateException("Assinatura não encontrada")

        // Validar se há valor a pagar
        val valor = assinatura.valor
                ?: throw IllegalStateException("Assinatura não possui valor definido")

        val periodoPagamento = assinatura.periodoPagamento
                ?: throw IllegalStateException("Assinatura não possui período de pagamento definido")

        // Criar registro de pagamento
        val pagamento = pagamentoRepository.save(
                Pagamento(
                        assinatura = assinatura,
                        valor = valor,
                        dataVencimento = assinatura.dataVencimento,
                        dataPagamento = LocalDateTime.now(),
                        status = StatusPagamento.PAGO,
                        metodoPagamento = request.metodoPagamento,
                        transacaoId = request.transacaoId
                )
        )

        // Atualizar assinatura
        val agora = LocalDateTime.now()
        val novaDataVencimento = assinaturaService.calcularProximaCobranca(agora, periodoPagamento)

        assinatura.status = StatusAssinatura.ATIVA
        assinatura.dataVencimento = novaDataVencimento
        assinatura.proximaCobranca = novaDataVencimento

        assinaturaRepository.save(assinatura)

        return ProcessarPagamentoResponse(
                sucesso = true,
                mensagem = "Pagamento processado com sucesso!",
                pagamento = PagamentoDto(
                        id = pagamento.id!!,
                        valor = pagamento.valor,
                        dataVencimento = pagamento.dataVencimento,
                        dataPagamento = pagamento.dataPagamento,
                        status = pagamento.status,
                        metodoPagamento = pagamento.metodoPagamento,
                        transacaoId = pagamento.transacaoId
                ),
                novaDataVencimento = novaDataVencimento
        )
    }
}
