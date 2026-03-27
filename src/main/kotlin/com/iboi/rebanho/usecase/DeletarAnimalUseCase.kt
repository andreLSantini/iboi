package com.iboi.rebanho.usecase

import com.iboi.rebanho.api.exception.AcessoNegadoException
import com.iboi.rebanho.api.exception.AnimalNaoEncontradoException
import com.iboi.rebanho.domain.StatusAnimal
import com.iboi.rebanho.repository.AnimalRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Component
class DeletarAnimalUseCase(
        private val animalRepository: AnimalRepository
) {

    @Transactional
    fun execute(animalId: UUID, farmId: UUID) {
        val animal = animalRepository.findById(animalId)
                .orElseThrow { AnimalNaoEncontradoException("Animal com ID $animalId não encontrado") }

        // Verificar multi-tenancy
        if (animal.farm.id != farmId) {
            throw AcessoNegadoException("Você não tem permissão para deletar este animal")
        }

        // Soft delete: apenas marca como DESCARTADO
        animal.status = StatusAnimal.DESCARTADO
        animal.atualizadoEm = LocalDateTime.now()
        animalRepository.save(animal)

        // Hard delete (alternativa, se preferir deletar completamente)
        // animalRepository.delete(animal)
    }
}
