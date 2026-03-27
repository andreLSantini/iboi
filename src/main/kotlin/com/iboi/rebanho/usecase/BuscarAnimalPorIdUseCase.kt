package com.iboi.rebanho.usecase

import com.iboi.rebanho.api.dto.AnimalDto
import com.iboi.rebanho.api.dto.toDto
import com.iboi.rebanho.api.exception.AcessoNegadoException
import com.iboi.rebanho.api.exception.AnimalNaoEncontradoException
import com.iboi.rebanho.repository.AnimalRepository
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class BuscarAnimalPorIdUseCase(
        private val animalRepository: AnimalRepository
) {

    fun execute(animalId: UUID, farmId: UUID): AnimalDto {
        val animal = animalRepository.findById(animalId)
                .orElseThrow { AnimalNaoEncontradoException("Animal com ID $animalId nao encontrado") }

        if (animal.farm.id != farmId) {
            throw AcessoNegadoException("Voce nao tem permissao para acessar este animal")
        }

        return animal.toDto()
    }
}
