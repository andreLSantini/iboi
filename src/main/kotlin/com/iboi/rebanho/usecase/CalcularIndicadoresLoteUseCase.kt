package com.iboi.rebanho.usecase

import com.iboi.rebanho.api.dto.GmdJanelaDto
import com.iboi.rebanho.domain.TipoEvento
import com.iboi.rebanho.repository.AnimalRepository
import com.iboi.rebanho.repository.EventoRepository
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.UUID

data class IndicadoresLote(
        val pesoMedioAtual: BigDecimal?,
        val gmdPorJanela: List<GmdJanelaDto>
)

@Component
class CalcularIndicadoresLoteUseCase(
        private val animalRepository: AnimalRepository,
        private val eventoRepository: EventoRepository
) {

    fun execute(loteId: UUID): IndicadoresLote {
        val animais = animalRepository.findByLoteId(loteId)
        val janelas = listOf(30, 60, 90)
        if (animais.isEmpty()) {
            return IndicadoresLote(
                    pesoMedioAtual = null,
                    gmdPorJanela = janelas.map { janela ->
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
            )
        }

        val pesosAtuais = animais.mapNotNull { it.pesoAtual }
        val pesoMedioAtual = if (pesosAtuais.isNotEmpty()) {
            pesosAtuais.fold(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(pesosAtuais.size.toLong()), 2, RoundingMode.HALF_UP)
        } else null

        val gmdPorJanela = janelas.map { janela ->
            val gmds = mutableListOf<BigDecimal>()
            val pesosIniciais = mutableListOf<BigDecimal>()
            val pesosFinais = mutableListOf<BigDecimal>()
            val variacoes = mutableListOf<BigDecimal>()
            val datasIniciais = mutableListOf<LocalDate>()
            val datasFinais = mutableListOf<LocalDate>()
            val diasList = mutableListOf<Long>()

            animais.forEach { animal ->
                val pesagens = eventoRepository.findByAnimalIdAndTipo(animal.id!!, TipoEvento.PESAGEM)
                        .filter { it.peso != null }
                        .sortedBy { it.data }

                val ultima = pesagens.lastOrNull() ?: return@forEach
                val dataCorte = ultima.data.minusDays(janela.toLong())
                val base = pesagens.filter { !it.data.isBefore(dataCorte) }.minByOrNull { it.data } ?: pesagens.firstOrNull()
                if (base == null || base.id == ultima.id) return@forEach

                val dias = ChronoUnit.DAYS.between(base.data, ultima.data)
                if (dias <= 0) return@forEach

                val pesoBase = base.peso ?: return@forEach
                val pesoFinal = ultima.peso ?: return@forEach
                val variacao = pesoFinal.subtract(pesoBase)
                val gmd = variacao.divide(BigDecimal.valueOf(dias), 3, RoundingMode.HALF_UP)

                pesosIniciais.add(pesoBase)
                pesosFinais.add(pesoFinal)
                variacoes.add(variacao)
                datasIniciais.add(base.data)
                datasFinais.add(ultima.data)
                diasList.add(dias)
                gmds.add(gmd)
            }

            GmdJanelaDto(
                    janelaDias = janela,
                    pesoInicial = pesosIniciais.averageOrNull(2),
                    pesoFinal = pesosFinais.averageOrNull(2),
                    dataInicial = datasIniciais.minOrNull(),
                    dataFinal = datasFinais.maxOrNull(),
                    diasConsiderados = diasList.average().takeIf { !it.isNaN() }?.toLong(),
                    variacaoPeso = variacoes.averageOrNull(2),
                    ganhoMedioDiario = gmds.averageOrNull(3)
            )
        }

        return IndicadoresLote(
                pesoMedioAtual = pesoMedioAtual,
                gmdPorJanela = gmdPorJanela
        )
    }

    private fun List<BigDecimal>.averageOrNull(scale: Int): BigDecimal? {
        if (isEmpty()) return null
        return fold(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(size.toLong()), scale, RoundingMode.HALF_UP)
    }
}
