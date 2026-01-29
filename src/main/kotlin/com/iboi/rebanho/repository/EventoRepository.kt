package com.iboi.rebanho.repository

import com.iboi.rebanho.domain.Evento
import com.iboi.rebanho.domain.TipoEvento
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate
import java.util.*

interface EventoRepository : JpaRepository<Evento, UUID> {

    fun findByAnimalIdOrderByDataDesc(animalId: UUID): List<Evento>

    fun findByFarmIdOrderByDataDesc(farmId: UUID): List<Evento>

    fun findByFarmIdAndTipoOrderByDataDesc(farmId: UUID, tipo: TipoEvento): List<Evento>

    @Query("SELECT e FROM Evento e WHERE e.farm.id = :farmId AND e.data BETWEEN :dataInicio AND :dataFim ORDER BY e.data DESC")
    fun findByFarmIdAndDataBetween(farmId: UUID, dataInicio: LocalDate, dataFim: LocalDate): List<Evento>

    @Query("SELECT e FROM Evento e WHERE e.animal.id = :animalId AND e.tipo = :tipo ORDER BY e.data DESC")
    fun findByAnimalIdAndTipo(animalId: UUID, tipo: TipoEvento): List<Evento>

    @Query("SELECT COUNT(e) FROM Evento e WHERE e.farm.id = :farmId AND e.tipo = :tipo")
    fun countByFarmIdAndTipo(farmId: UUID, tipo: TipoEvento): Long
}
