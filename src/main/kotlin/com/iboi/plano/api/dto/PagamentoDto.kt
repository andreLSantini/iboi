package com.iboi.plano.api.dto

import com.iboi.plano.model.MetodoPagamento
import com.iboi.plano.model.StatusPagamento
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

data class ProcessarPagamentoRequest(
        val metodoPagamento: MetodoPagamento,
        val transacaoId: String? = null
)

data class PagamentoDto(
        val id: UUID,
        val valor: BigDecimal,
        val dataVencimento: LocalDateTime,
        val dataPagamento: LocalDateTime?,
        val status: StatusPagamento,
        val metodoPagamento: MetodoPagamento?,
        val transacaoId: String?,
        val gatewayProvider: String?,
        val invoiceUrl: String?,
        val bankSlipUrl: String?,
        val pixPayload: String?,
        val pixEncodedImage: String?
)

data class ProcessarPagamentoResponse(
        val sucesso: Boolean,
        val mensagem: String,
        val pagamento: PagamentoDto?,
        val novaDataVencimento: LocalDateTime?
)
