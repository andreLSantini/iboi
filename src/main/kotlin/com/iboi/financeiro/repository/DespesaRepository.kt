package com.iboi.financeiro.repository

import com.iboi.financeiro.domain.CategoriaDespesa
import com.iboi.financeiro.domain.Despesa
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

interface DespesaRepository : JpaRepository<Despesa, UUID> {

    fun findByFarmIdOrderByDataDesc(farmId: UUID): List<Despesa>

    @Query("SELECT d FROM Despesa d WHERE d.farm.id = :farmId AND d.data BETWEEN :dataInicio AND :dataFim ORDER BY d.data DESC")
    fun findByFarmIdAndDataBetween(farmId: UUID, dataInicio: LocalDate, dataFim: LocalDate): List<Despesa>

    @Query("SELECT d FROM Despesa d WHERE d.farm.id = :farmId AND d.dataVencimento BETWEEN :dataInicio AND :dataFim ORDER BY d.dataVencimento ASC, d.data ASC")
    fun findByFarmIdAndDataVencimentoBetween(farmId: UUID, dataInicio: LocalDate, dataFim: LocalDate): List<Despesa>

    @Query("SELECT d FROM Despesa d WHERE d.farm.id = :farmId AND d.dataLiquidacao BETWEEN :dataInicio AND :dataFim ORDER BY d.dataLiquidacao ASC")
    fun findByFarmIdAndDataLiquidacaoBetween(farmId: UUID, dataInicio: LocalDate, dataFim: LocalDate): List<Despesa>

    @Query("SELECT d FROM Despesa d WHERE d.farm.id = :farmId AND d.categoria = :categoria ORDER BY d.data DESC")
    fun findByFarmIdAndCategoria(farmId: UUID, categoria: CategoriaDespesa): List<Despesa>

    @Query("SELECT SUM(d.valor) FROM Despesa d WHERE d.farm.id = :farmId AND d.data BETWEEN :dataInicio AND :dataFim")
    fun sumByFarmIdAndDataBetween(farmId: UUID, dataInicio: LocalDate, dataFim: LocalDate): BigDecimal?

    @Query("SELECT d.categoria, SUM(d.valor) FROM Despesa d WHERE d.farm.id = :farmId AND d.data BETWEEN :dataInicio AND :dataFim GROUP BY d.categoria")
    fun sumByFarmIdAndDataBetweenGroupByCategoria(farmId: UUID, dataInicio: LocalDate, dataFim: LocalDate): List<Array<Any>>
}
