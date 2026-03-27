package com.iboi.rebanho.repository

import com.iboi.rebanho.domain.Animal
import com.iboi.rebanho.domain.CategoriaAnimal
import com.iboi.rebanho.domain.Sexo
import com.iboi.rebanho.domain.StatusAnimal
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.*

interface AnimalRepository : JpaRepository<Animal, UUID> {

    @Query("SELECT COUNT(a) FROM Animal a WHERE a.farm.empresa.id = :empresaId")
    fun countByEmpresaId(@Param("empresaId") empresaId: UUID): Long

    fun findByFarmIdAndStatus(farmId: UUID, status: StatusAnimal): List<Animal>

    fun findByFarmId(farmId: UUID): List<Animal>

    fun findByBrincoAndFarmId(brinco: String, farmId: UUID): Animal?

    fun existsByBrincoAndFarmId(brinco: String, farmId: UUID): Boolean

    fun existsByRfidAndFarmId(rfid: String, farmId: UUID): Boolean

    fun existsByCodigoSisbov(codigoSisbov: String): Boolean

    fun findByLoteId(loteId: UUID): List<Animal>

    fun findByFarmIdAndCategoria(farmId: UUID, categoria: CategoriaAnimal): List<Animal>

    @Query("SELECT COUNT(a) FROM Animal a WHERE a.farm.id = :farmId AND a.status = :status")
    fun countByFarmIdAndStatus(farmId: UUID, status: StatusAnimal): Long

    @Query("SELECT a FROM Animal a WHERE a.farm.id = :farmId AND a.status = 'ATIVO' ORDER BY a.criadoEm DESC")
    fun findAtivosRecentesByFarmId(farmId: UUID): List<Animal>

    @Query("""
        SELECT a FROM Animal a WHERE a.farm.id = :farmId
        AND (:status IS NULL OR a.status = :status)
        AND (:categoria IS NULL OR a.categoria = :categoria)
        AND (:loteId IS NULL OR a.lote.id = :loteId)
        AND (:sexo IS NULL OR a.sexo = :sexo)
    """)
    fun findByFarmIdWithFilters(
            @Param("farmId") farmId: UUID,
            @Param("status") status: StatusAnimal?,
            @Param("categoria") categoria: CategoriaAnimal?,
            @Param("loteId") loteId: UUID?,
            @Param("sexo") sexo: Sexo?,
            pageable: Pageable
    ): Page<Animal>
}
