package com.iboi.financeiro.usecase

import com.iboi.financeiro.api.dto.ReceitaDto
import com.iboi.financeiro.api.dto.RegistrarReceitaRequest
import com.iboi.financeiro.domain.Receita
import com.iboi.financeiro.domain.StatusLancamentoFinanceiro
import com.iboi.financeiro.repository.ReceitaRepository
import com.iboi.identity.infrastructure.repository.FarmRepository
import com.iboi.identity.infrastructure.repository.UsuarioRepository
import com.iboi.rebanho.repository.AnimalRepository
import com.iboi.rebanho.repository.LoteRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.util.UUID

@Component
class RegistrarReceitaUseCase(
        private val receitaRepository: ReceitaRepository,
        private val farmRepository: FarmRepository,
        private val usuarioRepository: UsuarioRepository,
        private val animalRepository: AnimalRepository,
        private val loteRepository: LoteRepository
) {

    @Transactional
    fun execute(farmId: UUID, emailUsuario: String, request: RegistrarReceitaRequest): ReceitaDto {
        val farm = farmRepository.findById(farmId).orElseThrow {
            IllegalArgumentException("Fazenda nao encontrada")
        }

        val animal = request.animalId?.let { animalId ->
            animalRepository.findById(animalId).orElseThrow {
                IllegalArgumentException("Animal nao encontrado")
            }.also {
                if (it.farm.id != farmId) {
                    throw IllegalArgumentException("Animal nao pertence a fazenda informada")
                }
            }
        }

        val lote = request.loteId?.let { loteId ->
            loteRepository.findById(loteId).orElseThrow {
                IllegalArgumentException("Lote nao encontrado")
            }.also {
                if (it.farm.id != farmId) {
                    throw IllegalArgumentException("Lote nao pertence a fazenda informada")
                }
            }
        }

        val responsavel = usuarioRepository.findByEmail(emailUsuario)
        val dataVencimento = request.dataVencimento ?: request.data
        val status = resolverStatusReceita(request.status, dataVencimento, request.dataLiquidacao)
        val dataLiquidacao = when (status) {
            StatusLancamentoFinanceiro.RECEBIDO -> request.dataLiquidacao ?: request.data
            else -> request.dataLiquidacao
        }

        val receita = receitaRepository.save(
                Receita(
                        farm = farm,
                        tipo = request.tipo,
                        descricao = request.descricao,
                        valor = request.valor,
                        data = request.data,
                        dataVencimento = dataVencimento,
                        dataLiquidacao = dataLiquidacao,
                        formaPagamento = request.formaPagamento,
                        status = status,
                        lote = lote,
                        animal = animal,
                        responsavel = responsavel,
                        comprador = request.comprador,
                        quantidadeAnimais = request.quantidadeAnimais,
                        observacoes = request.observacoes
                )
        )

        return ReceitaDto(
                id = receita.id!!,
                tipo = receita.tipo,
                descricao = receita.descricao,
                valor = receita.valor,
                data = receita.data,
                dataVencimento = receita.dataVencimento,
                dataLiquidacao = receita.dataLiquidacao,
                status = receita.status,
                formaPagamento = receita.formaPagamento,
                comprador = receita.comprador,
                quantidadeAnimais = receita.quantidadeAnimais,
                responsavel = receita.responsavel?.nome,
                observacoes = receita.observacoes
        )
    }

    private fun resolverStatusReceita(
            statusSolicitado: StatusLancamentoFinanceiro?,
            dataVencimento: LocalDate,
            dataLiquidacao: LocalDate?
    ): StatusLancamentoFinanceiro {
        if (statusSolicitado == StatusLancamentoFinanceiro.PAGO) {
            throw IllegalArgumentException("Receita nao pode ser registrada com status PAGO")
        }

        return statusSolicitado ?: when {
            dataLiquidacao != null -> StatusLancamentoFinanceiro.RECEBIDO
            dataVencimento.isBefore(LocalDate.now()) -> StatusLancamentoFinanceiro.VENCIDO
            else -> StatusLancamentoFinanceiro.PENDENTE
        }
    }
}
