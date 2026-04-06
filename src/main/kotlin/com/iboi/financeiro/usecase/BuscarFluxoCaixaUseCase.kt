package com.iboi.financeiro.usecase

import com.iboi.financeiro.api.dto.FluxoCaixaDto
import com.iboi.financeiro.api.dto.FluxoCaixaResumoDto
import com.iboi.financeiro.api.dto.MovimentoFluxoCaixaDto
import com.iboi.financeiro.api.dto.TipoMovimentoCaixa
import com.iboi.financeiro.domain.Despesa
import com.iboi.financeiro.domain.Receita
import com.iboi.financeiro.domain.StatusLancamentoFinanceiro
import com.iboi.financeiro.repository.DespesaRepository
import com.iboi.financeiro.repository.ReceitaRepository
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

@Component
class BuscarFluxoCaixaUseCase(
        private val despesaRepository: DespesaRepository,
        private val receitaRepository: ReceitaRepository
) {

    fun execute(farmId: UUID, dataInicio: LocalDate, dataFim: LocalDate): FluxoCaixaDto {
        val despesasPrevistas = despesaRepository.findByFarmIdAndDataVencimentoBetween(farmId, dataInicio, dataFim)
        val receitasPrevistas = receitaRepository.findByFarmIdAndDataVencimentoBetween(farmId, dataInicio, dataFim)
        val despesasLiquidadas = despesaRepository.findByFarmIdAndDataLiquidacaoBetween(farmId, dataInicio, dataFim)
                .filter { it.status == StatusLancamentoFinanceiro.PAGO }
        val receitasLiquidadas = receitaRepository.findByFarmIdAndDataLiquidacaoBetween(farmId, dataInicio, dataFim)
                .filter { it.status == StatusLancamentoFinanceiro.RECEBIDO }

        val totalPrevistoReceber = receitasPrevistas.sumOfBigDecimal { it.valor }
        val totalPrevistoPagar = despesasPrevistas.sumOfBigDecimal { it.valor }
        val totalRecebido = receitasLiquidadas.sumOfBigDecimal { it.valor }
        val totalPago = despesasLiquidadas.sumOfBigDecimal { it.valor }
        val totalPendenteReceber = receitasPrevistas
                .filter { it.status == StatusLancamentoFinanceiro.PENDENTE || it.status == StatusLancamentoFinanceiro.VENCIDO }
                .sumOfBigDecimal { it.valor }
        val totalPendentePagar = despesasPrevistas
                .filter { it.status == StatusLancamentoFinanceiro.PENDENTE || it.status == StatusLancamentoFinanceiro.VENCIDO }
                .sumOfBigDecimal { it.valor }
        val totalVencido = receitasPrevistas
                .filter { it.status == StatusLancamentoFinanceiro.VENCIDO }
                .sumOfBigDecimal { it.valor }
                .add(
                        despesasPrevistas
                                .filter { it.status == StatusLancamentoFinanceiro.VENCIDO }
                                .sumOfBigDecimal { it.valor }
                )

        val movimentos = buildList {
            addAll(receitasPrevistas.map { it.toMovimento() })
            addAll(despesasPrevistas.map { it.toMovimento() })
        }.sortedWith(
                compareBy<MovimentoFluxoCaixaDto> { it.dataVencimento }
                        .thenBy { it.tipo.name }
                        .thenBy { it.descricao }
        )

        return FluxoCaixaDto(
                resumo = FluxoCaixaResumoDto(
                        dataInicio = dataInicio,
                        dataFim = dataFim,
                        totalRecebido = totalRecebido,
                        totalPago = totalPago,
                        totalPrevistoReceber = totalPrevistoReceber,
                        totalPrevistoPagar = totalPrevistoPagar,
                        totalPendenteReceber = totalPendenteReceber,
                        totalPendentePagar = totalPendentePagar,
                        saldoRealizado = totalRecebido.subtract(totalPago),
                        saldoProjetado = totalPrevistoReceber.subtract(totalPrevistoPagar),
                        totalVencido = totalVencido
                ),
                movimentos = movimentos
        )
    }

    private fun Receita.toMovimento(): MovimentoFluxoCaixaDto = MovimentoFluxoCaixaDto(
            id = id!!,
            tipo = TipoMovimentoCaixa.ENTRADA,
            descricao = descricao,
            origem = tipo.name,
            valor = valor,
            dataCompetencia = data,
            dataVencimento = dataVencimento,
            dataLiquidacao = dataLiquidacao,
            status = status,
            formaPagamento = formaPagamento.name,
            contraparte = comprador
    )

    private fun Despesa.toMovimento(): MovimentoFluxoCaixaDto = MovimentoFluxoCaixaDto(
            id = id!!,
            tipo = TipoMovimentoCaixa.SAIDA,
            descricao = descricao,
            origem = categoria.name,
            valor = valor,
            dataCompetencia = data,
            dataVencimento = dataVencimento,
            dataLiquidacao = dataLiquidacao,
            status = status,
            formaPagamento = formaPagamento.name,
            contraparte = responsavel?.nome
    )

    private inline fun <T> Iterable<T>.sumOfBigDecimal(selector: (T) -> BigDecimal): BigDecimal =
            fold(BigDecimal.ZERO) { acc, item -> acc.add(selector(item)) }
}
