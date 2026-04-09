package com.iboi.rebanho.usecase

import com.iboi.rebanho.api.dto.LoteDto
import com.iboi.rebanho.domain.Lote
import com.iboi.rebanho.repository.AnimalRepository
import com.iboi.rebanho.repository.LoteRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import java.util.*

@Component
class ListarLotesUseCase(
        private val loteRepository: LoteRepository,
        private val animalRepository: AnimalRepository,
        private val calcularIndicadoresLoteUseCase: CalcularIndicadoresLoteUseCase
) {

    fun execute(farmId: UUID, apenasAtivos: Boolean?, pageable: Pageable): Page<LoteDto> {
        val lotes = if (apenasAtivos == true) {
            loteRepository.findByFarmIdAndAtivo(farmId, true, pageable)
        } else {
            loteRepository.findByFarmId(farmId, pageable)
        }

        return lotes.map { toDto(it) }
    }

    private fun toDto(lote: Lote): LoteDto {
        val quantidade = animalRepository.findByLoteId(lote.id!!).size
        val indicadores = calcularIndicadoresLoteUseCase.execute(lote.id)

        return LoteDto(
                id = lote.id!!,
                nome = lote.nome,
                descricao = lote.descricao,
                ativo = lote.ativo,
                quantidadeAnimais = quantidade,
                pesoMedioAtual = indicadores.pesoMedioAtual,
                gmdPorJanela = indicadores.gmdPorJanela,
                criadoEm = lote.criadoEm
        )
    }
}
