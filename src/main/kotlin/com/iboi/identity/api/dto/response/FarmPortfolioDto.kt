package com.iboi.identity.api.dto.response

import java.util.UUID

data class MultiFarmPortfolioDto(
        val resumo: MultiFarmResumoDto,
        val fazendas: List<FarmOperacionalDto>
)

data class MultiFarmResumoDto(
        val totalFazendas: Int,
        val fazendasAtivas: Int,
        val totalAnimaisAtivos: Long,
        val totalLotesAtivos: Int,
        val totalPastos: Int,
        val areaTotalHa: Double,
        val areaPastagemHa: Double,
        val areaProdutivaHa: Double
)

data class FarmOperacionalDto(
        val id: UUID,
        val nome: String,
        val cidade: String?,
        val estado: String?,
        val tipoProducao: String,
        val ativa: Boolean,
        val areaTotalHa: Double?,
        val areaPastagemHa: Double?,
        val areaProdutivaHa: Double?,
        val capacidadeEstimada: Int?,
        val animaisAtivos: Long,
        val lotesAtivos: Int,
        val totalPastos: Int,
        val totalAreaPastosHa: Double,
        val taxaOcupacaoEstimada: Double?,
        val areasOperacionais: List<AreaOperacionalDto>
)

data class AreaOperacionalDto(
        val id: UUID,
        val nome: String,
        val areaHa: Double?,
        val ativa: Boolean,
        val animaisAtivos: Int
)
