package com.iboi.rebanho.usecase

import com.iboi.rebanho.api.dto.GmdJanelaDto
import com.iboi.rebanho.api.dto.calcularGanhoMedioDiario
import com.iboi.rebanho.domain.TipoEvento
import com.iboi.rebanho.repository.EventoRepository
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.temporal.ChronoUnit
import java.util.UUID

@Component
class CalcularGmdPorJanelaUseCase(
        private val eventoRepository: EventoRepository
) {

    fun execute(animalId: UUID): List<GmdJanelaDto> {
        val pesagens = eventoRepository.findByAnimalIdAndTipo(animalId, TipoEvento.PESAGEM)
                .filter { it.peso != null }
                .sortedBy { it.data }

        val ultimaPesagem = pesagens.lastOrNull()
                ?: return listOf(7, 30, 60, 90).map { janela ->
                    GmdJanelaDto(
                            janelaDias = janela,
                            pesoInicial = null,
                            pesoFinal = null,
                            dataInicial = null,
                            dataFinal = null,
                            diasConsiderados = null,
                            variacaoPeso = null,
                            ganhoMedioDiario = null
                    )
                }

        return listOf(7, 30, 60, 90).map { janela ->
            val dataCorte = ultimaPesagem.data.minusDays(janela.toLong())
            val pesagemBase = pesagens
                    .filter { !it.data.isBefore(dataCorte) }
                    .minByOrNull { it.data }
                    ?: pesagens.first()

            val diasConsiderados = ChronoUnit.DAYS.between(pesagemBase.data, ultimaPesagem.data)
            val variacaoPeso = if (diasConsiderados > 0) {
                ultimaPesagem.peso!!.subtract(pesagemBase.peso)
            } else null

            GmdJanelaDto(
                    janelaDias = janela,
                    pesoInicial = pesagemBase.peso,
                    pesoFinal = ultimaPesagem.peso,
                    dataInicial = pesagemBase.data,
                    dataFinal = ultimaPesagem.data,
                    diasConsiderados = diasConsiderados.takeIf { it > 0 },
                    variacaoPeso = variacaoPeso,
                    ganhoMedioDiario = if (variacaoPeso != null) {
                        calcularGanhoMedioDiario(variacaoPeso, diasConsiderados)
                    } else null
            )
        }
    }
}
