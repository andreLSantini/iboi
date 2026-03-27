package com.iboi.rebanho.repository

import com.iboi.rebanho.domain.VacinacaoAnimal
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface VacinacaoAnimalRepository : JpaRepository<VacinacaoAnimal, UUID> {
    fun findByAnimalIdOrderByAplicadaEmDescCriadoEmDesc(animalId: UUID): List<VacinacaoAnimal>
}
