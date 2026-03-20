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
import com.iboi.plano.service.BillingGateway
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Component
class ProcessarPagamentoUseCase(
        private val assinaturaRepository: AssinaturaRepository,
        private val pagamentoRepository: PagamentoRepository,
        private val assinaturaService: AssinaturaService,
        private val billingGateway: BillingGateway
) {

    @Transactional
    fun execute(empresaId: UUID, request: ProcessarPagamentoRequest): ProcessarPagamentoResponse {
        val assinatura = assinaturaRepository.findByEmpresaId(empresaId)
                ?: throw IllegalStateException("Assinatura nao encontrada")

        val valor = assinatura.valor
                ?: throw IllegalStateException("Assinatura nao possui valor definido")

        val periodoPagamento = assinatura.periodoPagamento
                ?: throw IllegalStateException("Assinatura nao possui periodo de pagamento definido")

        val gatewayResult = billingGateway.capturePayment(
                empresaId = empresaId,
                valor = valor,
                metodoPagamento = request.metodoPagamento,
                externalTransactionId = request.transacaoId
        )

        if (!gatewayResult.success) {
            throw IllegalStateException("Nao foi possivel confirmar o pagamento no gateway")
        }

        val pagamento = pagamentoRepository.save(
                Pagamento(
                        assinatura = assinatura,
                        valor = valor,
                        dataVencimento = assinatura.dataVencimento,
                        dataPagamento = LocalDateTime.now(),
                        status = StatusPagamento.PAGO,
                        metodoPagamento = request.metodoPagamento,
                        transacaoId = gatewayResult.transactionId
                )
        )

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
