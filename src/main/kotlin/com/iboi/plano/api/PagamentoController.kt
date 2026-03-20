package com.iboi.plano.api

import com.iboi.plano.api.dto.PagamentoDto
import com.iboi.plano.api.dto.ProcessarPagamentoRequest
import com.iboi.plano.api.dto.ProcessarPagamentoResponse
import com.iboi.plano.repository.PagamentoRepository
import com.iboi.plano.usecase.ProcessarPagamentoUseCase
import com.iboi.shared.security.SecurityUtils
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/pagamento")
class PagamentoController(
        private val pagamentoRepository: PagamentoRepository,
        private val processarPagamentoUseCase: ProcessarPagamentoUseCase
) {

    @PostMapping("/processar")
    fun processar(@RequestBody request: ProcessarPagamentoRequest): ResponseEntity<ProcessarPagamentoResponse> {
        val response = processarPagamentoUseCase.execute(SecurityUtils.currentEmpresaId(), request)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/historico")
    fun historico(): ResponseEntity<List<PagamentoDto>> {
        val pagamentos = pagamentoRepository.findByAssinaturaEmpresaIdOrderByDataVencimentoDesc(
                SecurityUtils.currentEmpresaId()
        )

        val dtos = pagamentos.map { pagamento ->
            PagamentoDto(
                    id = pagamento.id!!,
                    valor = pagamento.valor,
                    dataVencimento = pagamento.dataVencimento,
                    dataPagamento = pagamento.dataPagamento,
                    status = pagamento.status,
                    metodoPagamento = pagamento.metodoPagamento,
                    transacaoId = pagamento.transacaoId,
                    gatewayProvider = pagamento.gatewayProvider,
                    invoiceUrl = pagamento.invoiceUrl,
                    bankSlipUrl = pagamento.bankSlipUrl,
                    pixPayload = pagamento.pixPayload,
                    pixEncodedImage = pagamento.pixEncodedImage
            )
        }

        return ResponseEntity.ok(dtos)
    }
}
