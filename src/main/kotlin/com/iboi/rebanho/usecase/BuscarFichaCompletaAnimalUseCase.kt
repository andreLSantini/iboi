package com.iboi.rebanho.usecase

import com.iboi.rebanho.api.dto.AnimalFichaCompletaDto
import com.iboi.rebanho.api.dto.toDto
import com.iboi.rebanho.api.exception.AcessoNegadoException
import com.iboi.rebanho.api.exception.AnimalNaoEncontradoException
import com.iboi.rebanho.domain.TipoEvento
import com.iboi.rebanho.repository.AnimalRepository
import com.iboi.rebanho.repository.EventoRepository
import com.iboi.rebanho.repository.MovimentacaoAnimalRepository
import com.iboi.rebanho.repository.VacinacaoAnimalRepository
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class BuscarFichaCompletaAnimalUseCase(
        private val animalRepository: AnimalRepository,
        private val listarPesagensAnimalUseCase: ListarPesagensAnimalUseCase,
        private val calcularGmdPorJanelaUseCase: CalcularGmdPorJanelaUseCase,
        private val eventoRepository: EventoRepository,
        private val vacinacaoAnimalRepository: VacinacaoAnimalRepository,
        private val movimentacaoAnimalRepository: MovimentacaoAnimalRepository
) {

    fun execute(animalId: UUID, farmId: UUID): AnimalFichaCompletaDto {
        val animal = animalRepository.findById(animalId)
                .orElseThrow { AnimalNaoEncontradoException("Animal com ID $animalId nao encontrado") }

        if (animal.farm.id != farmId) {
            throw AcessoNegadoException("Voce nao tem permissao para acessar este animal")
        }

        return AnimalFichaCompletaDto(
                animal = animal.toDto(),
                pesagens = listarPesagensAnimalUseCase.execute(animalId, farmId),
                gmdPorJanela = calcularGmdPorJanelaUseCase.execute(animalId),
                eventos = eventoRepository.findByAnimalIdOrderByDataDesc(animalId).map { it.toDto() },
                eventosReprodutivos = eventoRepository.findByAnimalIdOrderByDataDesc(animalId)
                        .filter {
                            it.tipo in setOf(
                                    TipoEvento.INSEMINACAO,
                                    TipoEvento.COBERTURA,
                                    TipoEvento.DIAGNOSTICO_GESTACAO,
                                    TipoEvento.PARTO
                            )
                        }
                        .map { it.toDto() },
                vacinacoes = vacinacaoAnimalRepository.findByAnimalIdOrderByAplicadaEmDescCriadoEmDesc(animalId).map { it.toDto() },
                movimentacoes = movimentacaoAnimalRepository.findByAnimalIdOrderByMovimentadaEmDescCriadoEmDesc(animalId).map { it.toDto() }
        )
    }
}
