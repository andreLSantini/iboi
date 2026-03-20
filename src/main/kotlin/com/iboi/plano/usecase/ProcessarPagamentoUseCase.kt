package com.iboi.plano.usecase

import com.iboi.plano.api.dto.PagamentoDto
import com.iboi.plano.api.dto.ProcessarPagamentoRequest
import com.iboi.plano.api.dto.ProcessarPagamentoResponse
import com.iboi.plano.model.Pagamento
import com.iboi.plano.model.StatusPagamento
import com.iboi.plano.repository.AssinaturaRepository
import com.iboi.plano.repository.PagamentoRepository
import com.iboi.plano.service.BillingGateway
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Component
class ProcessarPagamentoUseCase(
        private val assinaturaRepository: AssinaturaRepository,
        private val pagamentoRepository: PagamentoRepository,
        private val billingGateway: BillingGateway
) {

    @Transactional
    fun execute(empresaId: UUID, request: ProcessarPagamentoRequest): ProcessarPagamentoResponse {
        val assinatura = assinaturaRepository.findByEmpresaId(empresaId)
                ?: throw IllegalStateException("Assinatura nao encontrada")

        val valor = assinatura.valor
                ?: throw IllegalStateException("Assinatura nao possui valor definido")

        val dueDate = assinatura.dataVencimento.toLocalDate()
        val description = "Plano ${assinatura.tipo.name} - ${assinatura.periodoPagamento?.name ?: "MENSAL"}"

        val charge = billingGateway.createCharge(
                empresa = assinatura.empresa,
                valor = valor,
                metodoPagamento = request.metodoPagamento,
                dueDate = dueDate,
                description = description
        )

        val pagamento = pagamentoRepository.save(
                Pagamento(
                        assinatura = assinatura,
                        valor = valor,
                        dataVencimento = assinatura.dataVencimento,
                        status = if (charge.success) StatusPagamento.PENDENTE else StatusPagamento.CANCELADO,
                        metodoPagamento = request.metodoPagamento,
                        transacaoId = charge.transactionId,
                        gatewayProvider = charge.provider,
                        invoiceUrl = charge.invoiceUrl,
                        bankSlipUrl = charge.bankSlipUrl,
                        pixPayload = charge.pixPayload,
                        pixEncodedImage = charge.pixEncodedImage
                )
        )

        return ProcessarPagamentoResponse(
                sucesso = charge.success,
                mensagem = "Cobranca criada com sucesso. Conclua o pagamento no Asaas para ativar o plano.",
                pagamento = pagamento.toDto(),
                novaDataVencimento = assinatura.proximaCobranca
        )
    }

    private fun Pagamento.toDto() = PagamentoDto(
            id = id!!,
            valor = valor,
            dataVencimento = dataVencimento,
            dataPagamento = dataPagamento,
            status = status,
            metodoPagamento = metodoPagamento,
            transacaoId = transacaoId,
            gatewayProvider = gatewayProvider,
            invoiceUrl = invoiceUrl,
            bankSlipUrl = bankSlipUrl,
            pixPayload = pixPayload,
            pixEncodedImage = pixEncodedImage
    )
}
