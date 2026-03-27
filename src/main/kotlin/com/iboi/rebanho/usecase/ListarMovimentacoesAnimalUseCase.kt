package com.iboi.rebanho.usecase

import com.iboi.rebanho.api.dto.MovimentacaoAnimalDto
import com.iboi.rebanho.api.dto.toDto
import com.iboi.rebanho.repository.AnimalRepository
import com.iboi.rebanho.repository.MovimentacaoAnimalRepository
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class ListarMovimentacoesAnimalUseCase(
        private val animalRepository: AnimalRepository,
        private val movimentacaoAnimalRepository: MovimentacaoAnimalRepository
) {

    fun execute(animalId: UUID, farmId: UUID): List<MovimentacaoAnimalDto> {
        val animal = animalRepository.findById(animalId).orElseThrow {
            IllegalArgumentException("Animal nao encontrado")
        }

        if (animal.farm.id != farmId && movimentacaoAnimalRepository.findByAnimalIdOrderByMovimentadaEmDescCriadoEmDesc(animalId)
                        .none { it.farmOrigem?.id == farmId || it.farmDestino?.id == farmId }) {
            throw IllegalArgumentException("Animal nao pertence ao contexto permitido")
        }

        return movimentacaoAnimalRepository.findByAnimalIdOrderByMovimentadaEmDescCriadoEmDesc(animalId)
                .map { it.toDto() }
    }
}
