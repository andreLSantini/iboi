package com.iboi.financeiro.usecase

import com.iboi.financeiro.api.dto.DespesaDto
import com.iboi.financeiro.api.dto.RegistrarDespesaRequest
import com.iboi.financeiro.domain.Despesa
import com.iboi.financeiro.domain.StatusLancamentoFinanceiro
import com.iboi.financeiro.repository.DespesaRepository
import com.iboi.identity.infrastructure.repository.FarmRepository
import com.iboi.identity.infrastructure.repository.UsuarioRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.util.*

@Component
class RegistrarDespesaUseCase(
        private val despesaRepository: DespesaRepository,
        private val farmRepository: FarmRepository,
        private val usuarioRepository: UsuarioRepository
) {

    @Transactional
    fun execute(farmId: UUID, emailUsuario: String, request: RegistrarDespesaRequest): DespesaDto {
        val farm = farmRepository.findById(farmId).orElseThrow {
            IllegalArgumentException("Fazenda não encontrada")
        }

        val responsavel = usuarioRepository.findByEmail(emailUsuario)
        val dataVencimento = request.dataVencimento ?: request.data
        val status = resolverStatusDespesa(request.status, dataVencimento, request.dataLiquidacao)
        val dataLiquidacao = when (status) {
            StatusLancamentoFinanceiro.PAGO -> request.dataLiquidacao ?: request.data
            else -> request.dataLiquidacao
        }

        val despesa = despesaRepository.save(
                Despesa(
                        farm = farm,
                        categoria = request.categoria,
                        descricao = request.descricao,
                        valor = request.valor,
                        data = request.data,
                        dataVencimento = dataVencimento,
                        dataLiquidacao = dataLiquidacao,
                        formaPagamento = request.formaPagamento,
                        status = status,
                        responsavel = responsavel,
                        observacoes = request.observacoes
                )
        )

        return DespesaDto(
                id = despesa.id!!,
                categoria = despesa.categoria,
                descricao = despesa.descricao,
                valor = despesa.valor,
                data = despesa.data,
                dataVencimento = despesa.dataVencimento,
                dataLiquidacao = despesa.dataLiquidacao,
                status = despesa.status,
                formaPagamento = despesa.formaPagamento,
                responsavel = despesa.responsavel?.nome,
                observacoes = despesa.observacoes
        )
    }

    private fun resolverStatusDespesa(
            statusSolicitado: StatusLancamentoFinanceiro?,
            dataVencimento: LocalDate,
            dataLiquidacao: LocalDate?
    ): StatusLancamentoFinanceiro {
        if (statusSolicitado == StatusLancamentoFinanceiro.RECEBIDO) {
            throw IllegalArgumentException("Despesa nao pode ser registrada com status RECEBIDO")
        }

        return statusSolicitado ?: when {
            dataLiquidacao != null -> StatusLancamentoFinanceiro.PAGO
            dataVencimento.isBefore(LocalDate.now()) -> StatusLancamentoFinanceiro.VENCIDO
            else -> StatusLancamentoFinanceiro.PENDENTE
        }
    }
}
