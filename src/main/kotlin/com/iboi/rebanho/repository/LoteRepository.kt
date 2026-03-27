package com.iboi.rebanho.repository

import com.iboi.rebanho.domain.Lote
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface LoteRepository : JpaRepository<Lote, UUID> {

    fun findByFarmId(farmId: UUID): List<Lote>

    fun findByFarmId(farmId: UUID, pageable: Pageable): Page<Lote>

    fun findByFarmIdAndAtivo(farmId: UUID, ativo: Boolean): List<Lote>

    fun findByFarmIdAndAtivo(farmId: UUID, ativo: Boolean, pageable: Pageable): Page<Lote>

    fun existsByNomeAndFarmId(nome: String, farmId: UUID): Boolean

    fun findByFarmIdAndNomeIgnoreCase(farmId: UUID, nome: String): Lote?
}
