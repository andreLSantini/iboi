package com.iboi.agenda.api.dto

import com.iboi.rebanho.api.dto.AnimalResumoDto
import com.iboi.rebanho.api.dto.LoteResumoDto
import java.time.LocalDate

data class AgendaOperacionalItemDto(
        val id: String,
        val categoria: CategoriaAgenda,
        val origem: OrigemAgenda,
        val titulo: String,
        val descricao: String,
        val dataPrevista: LocalDate,
        val situacao: SituacaoAgenda,
        val prioridade: PrioridadeAgenda,
        val diasParaVencimento: Long,
        val animal: AnimalResumoDto? = null,
        val lote: LoteResumoDto? = null
)

data class AgendaOperacionalResumoDto(
        val total: Int,
        val atrasados: Int,
        val hoje: Int,
        val proximos7Dias: Int
)

data class AgendaOperacionalResponse(
        val resumo: AgendaOperacionalResumoDto,
        val itens: List<AgendaOperacionalItemDto>
)

enum class CategoriaAgenda {
    SANITARIO,
    PESAGEM,
    REPRODUCAO
}

enum class OrigemAgenda {
    AGENDAMENTO,
    HEURISTICA
}

enum class SituacaoAgenda {
    ATRASADO,
    HOJE,
    PROXIMO
}

enum class PrioridadeAgenda {
    ALTA,
    MEDIA,
    BAIXA
}
