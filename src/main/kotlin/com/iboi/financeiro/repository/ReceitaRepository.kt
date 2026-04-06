package com.iboi.financeiro.repository

import com.iboi.financeiro.domain.Receita
import com.iboi.financeiro.domain.TipoReceita
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

interface ReceitaRepository : JpaRepository<Receita, UUID> {

    fun findByFarmIdOrderByDataDesc(farmId: UUID): List<Receita>

    @Query("SELECT r FROM Receita r WHERE r.farm.id = :farmId AND r.data BETWEEN :dataInicio AND :dataFim ORDER BY r.data DESC")
    fun findByFarmIdAndDataBetween(farmId: UUID, dataInicio: LocalDate, dataFim: LocalDate): List<Receita>

    @Query("SELECT r FROM Receita r WHERE r.farm.id = :farmId AND r.dataVencimento BETWEEN :dataInicio AND :dataFim ORDER BY r.dataVencimento ASC, r.data ASC")
    fun findByFarmIdAndDataVencimentoBetween(farmId: UUID, dataInicio: LocalDate, dataFim: LocalDate): List<Receita>

    @Query("SELECT r FROM Receita r WHERE r.farm.id = :farmId AND r.dataLiquidacao BETWEEN :dataInicio AND :dataFim ORDER BY r.dataLiquidacao ASC")
    fun findByFarmIdAndDataLiquidacaoBetween(farmId: UUID, dataInicio: LocalDate, dataFim: LocalDate): List<Receita>

    @Query("SELECT SUM(r.valor) FROM Receita r WHERE r.farm.id = :farmId AND r.data BETWEEN :dataInicio AND :dataFim")
    fun sumByFarmIdAndDataBetween(farmId: UUID, dataInicio: LocalDate, dataFim: LocalDate): BigDecimal?

    @Query("SELECT r.tipo, SUM(r.valor) FROM Receita r WHERE r.farm.id = :farmId AND r.data BETWEEN :dataInicio AND :dataFim GROUP BY r.tipo")
    fun sumByFarmIdAndDataBetweenGroupByTipo(farmId: UUID, dataInicio: LocalDate, dataFim: LocalDate): List<Array<Any>>

    fun findByFarmIdAndTipoOrderByDataDesc(farmId: UUID, tipo: TipoReceita): List<Receita>
}
