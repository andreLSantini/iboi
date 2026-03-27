package com.iboi.rebanho.usecase

import com.iboi.rebanho.api.dto.VacinacaoAnimalDto
import com.iboi.rebanho.api.dto.toDto
import com.iboi.rebanho.repository.AnimalRepository
import com.iboi.rebanho.repository.VacinacaoAnimalRepository
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class ListarVacinacoesAnimalUseCase(
        private val animalRepository: AnimalRepository,
        private val vacinacaoAnimalRepository: VacinacaoAnimalRepository
) {

    fun execute(animalId: UUID, farmId: UUID): List<VacinacaoAnimalDto> {
        val animal = animalRepository.findById(animalId).orElseThrow {
            IllegalArgumentException("Animal nao encontrado")
        }

        if (animal.farm.id != farmId) {
            throw IllegalArgumentException("Animal nao pertence a fazenda ativa")
        }

        return vacinacaoAnimalRepository.findByAnimalIdOrderByAplicadaEmDescCriadoEmDesc(animalId)
                .map { it.toDto() }
    }
}
