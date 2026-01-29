package com.iboi.sanitario.repository

import com.iboi.sanitario.domain.AgendamentoSanitario
import com.iboi.sanitario.domain.StatusAgendamento
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate
import java.util.*

interface AgendamentoSanitarioRepository : JpaRepository<AgendamentoSanitario, UUID> {

    fun findByAnimalIdAndStatus(animalId: UUID, status: StatusAgendamento): List<AgendamentoSanitario>

    @Query("SELECT a FROM AgendamentoSanitario a WHERE a.animal.farm.id = :farmId AND a.status = :status ORDER BY a.dataPrevista ASC")
    fun findByFarmIdAndStatus(farmId: UUID, status: StatusAgendamento): List<AgendamentoSanitario>

    @Query("SELECT a FROM AgendamentoSanitario a WHERE a.animal.farm.id = :farmId AND a.dataPrevista BETWEEN :dataInicio AND :dataFim ORDER BY a.dataPrevista ASC")
    fun findByFarmIdAndDataPrevistaBetween(farmId: UUID, dataInicio: LocalDate, dataFim: LocalDate): List<AgendamentoSanitario>

    @Query("SELECT a FROM AgendamentoSanitario a WHERE a.status = 'PENDENTE' AND a.dataPrevista < :hoje")
    fun findAtrasados(hoje: LocalDate): List<AgendamentoSanitario>
}
