package com.iboi.rebanho.repository

import com.iboi.rebanho.domain.Animal
import com.iboi.rebanho.domain.CategoriaAnimal
import com.iboi.rebanho.domain.StatusAnimal
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.*

interface AnimalRepository : JpaRepository<Animal, UUID> {

    fun findByFarmIdAndStatus(farmId: UUID, status: StatusAnimal): List<Animal>

    fun findByFarmId(farmId: UUID): List<Animal>

    fun findByBrincoAndFarmId(brinco: String, farmId: UUID): Animal?

    fun existsByBrincoAndFarmId(brinco: String, farmId: UUID): Boolean

    fun findByLoteId(loteId: UUID): List<Animal>

    fun findByFarmIdAndCategoria(farmId: UUID, categoria: CategoriaAnimal): List<Animal>

    @Query("SELECT COUNT(a) FROM Animal a WHERE a.farm.id = :farmId AND a.status = :status")
    fun countByFarmIdAndStatus(farmId: UUID, status: StatusAnimal): Long

    @Query("SELECT a FROM Animal a WHERE a.farm.id = :farmId AND a.status = 'ATIVO' ORDER BY a.criadoEm DESC")
    fun findAtivosRecentesByFarmId(farmId: UUID): List<Animal>
}
