package com.iboi.rebanho.repository

import com.iboi.rebanho.domain.MovimentacaoAnimal
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface MovimentacaoAnimalRepository : JpaRepository<MovimentacaoAnimal, UUID> {
    fun findByAnimalIdOrderByMovimentadaEmDescCriadoEmDesc(animalId: UUID): List<MovimentacaoAnimal>
}
