package com.iboi.plano.api

import com.iboi.plano.api.dto.PagamentoDto
import com.iboi.plano.api.dto.ProcessarPagamentoRequest
import com.iboi.plano.api.dto.ProcessarPagamentoResponse
import com.iboi.identity.infrastructure.repository.UsuarioRepository
import com.iboi.plano.repository.PagamentoRepository
import com.iboi.plano.usecase.ProcessarPagamentoUseCase
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/pagamento")
class PagamentoController(
        private val pagamentoRepository: PagamentoRepository,
        private val usuarioRepository: UsuarioRepository,
        private val processarPagamentoUseCase: ProcessarPagamentoUseCase
) {

    @PostMapping("/processar")
    fun processar(@RequestBody request: ProcessarPagamentoRequest): ResponseEntity<ProcessarPagamentoResponse> {
        val empresaId = getEmpresaIdFromAuth()
        val response = processarPagamentoUseCase.execute(empresaId, request)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/historico")
    fun historico(): ResponseEntity<List<PagamentoDto>> {
        val empresaId = getEmpresaIdFromAuth()
        val pagamentos = pagamentoRepository.findByAssinaturaEmpresaIdOrderByDataVencimentoDesc(empresaId)

        val dtos = pagamentos.map { pagamento ->
            PagamentoDto(
                    id = pagamento.id!!,
                    valor = pagamento.valor,
                    dataVencimento = pagamento.dataVencimento,
                    dataPagamento = pagamento.dataPagamento,
                    status = pagamento.status,
                    metodoPagamento = pagamento.metodoPagamento,
                    transacaoId = pagamento.transacaoId
            )
        }

        return ResponseEntity.ok(dtos)
    }

    private fun getEmpresaIdFromAuth(): java.util.UUID {
        val email = SecurityContextHolder.getContext().authentication.principal as String
        val usuario = usuarioRepository.findByEmail(email)
                ?: throw IllegalStateException("Usuário não encontrado")
        return usuario.empresa.id!!
    }
}
