package com.iboi.relatorio.dto

import com.iboi.rebanho.domain.CategoriaAnimal
import com.iboi.rebanho.domain.Sexo
import com.iboi.rebanho.domain.StatusAnimal
import com.iboi.rebanho.domain.TipoEvento
import java.math.BigDecimal
import java.time.LocalDate

// Relatório de Rebanho
data class RelatorioRebanhoResponse(
        val totalAnimais: Long,
        val porCategoria: Map<CategoriaAnimal, Long>,
        val porSexo: Map<Sexo, Long>,
        val porStatus: Map<StatusAnimal, Long>,
        val idadeMediaMeses: Int,
        val pesoMedio: BigDecimal?
)

// Relatório Financeiro
data class RelatorioFinanceiroResponse(
        val periodo: PeriodoDto,
        val totalDespesas: BigDecimal,
        val despesasPorCategoria: List<DespesaCategoriaDto>,
        val comparativoMesAnterior: ComparativoDto?
)

data class PeriodoDto(
        val dataInicio: LocalDate,
        val dataFim: LocalDate
)

data class DespesaCategoriaDto(
        val categoria: String,
        val total: BigDecimal,
        val percentual: Double
)

data class ComparativoDto(
        val totalMesAnterior: BigDecimal,
        val variacao: BigDecimal,
        val variacaoPercentual: Double
)

// Histórico do Animal
data class HistoricoAnimalResponse(
        val animalId: String,
        val brinco: String,
        val nome: String?,
        val timeline: List<EventoTimelineDto>,
        val evolucaoPeso: List<PesoDto>,
        val totalEventos: Long
)

data class EventoTimelineDto(
        val data: LocalDate,
        val tipo: TipoEvento,
        val descricao: String
)

data class PesoDto(
        val data: LocalDate,
        val peso: BigDecimal
)

// Dashboard
data class DashboardResponse(
        val kpis: KpisDto,
        val eventosRecentes: List<EventoRecenteDto>,
        val agendamentosProximos: List<AgendamentoProximoDto>
)

data class KpisDto(
        val totalAnimaisAtivos: Long,
        val nascimentosMes: Long,
        val mortesMes: Long,
        val despesasMes: BigDecimal,
        val animaisPorCategoria: Map<String, Long>
)

data class EventoRecenteDto(
        val data: LocalDate,
        val tipo: String,
        val animal: String,
        val descricao: String
)

data class AgendamentoProximoDto(
        val dataPrevista: LocalDate,
        val tipo: String,
        val animal: String,
        val produto: String
)
