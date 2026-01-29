package com.iboi.ia.repository

import com.iboi.ia.domain.Alerta
import com.iboi.ia.domain.StatusAlerta
import com.iboi.ia.domain.PrioridadeAlerta
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.*

interface AlertaRepository : JpaRepository<Alerta, UUID> {

    fun findByFarmIdAndStatusOrderByPrioridadeDescCriadoEmDesc(farmId: UUID, status: StatusAlerta): List<Alerta>

    fun findByFarmIdOrderByCriadoEmDesc(farmId: UUID): List<Alerta>

    @Query("SELECT COUNT(a) FROM Alerta a WHERE a.farm.id = :farmId AND a.status = :status")
    fun countByFarmIdAndStatus(farmId: UUID, status: StatusAlerta): Long

    @Query("SELECT COUNT(a) FROM Alerta a WHERE a.farm.id = :farmId AND a.status = :status AND a.prioridade = :prioridade")
    fun countByFarmIdAndStatusAndPrioridade(farmId: UUID, status: StatusAlerta, prioridade: PrioridadeAlerta): Long
}
