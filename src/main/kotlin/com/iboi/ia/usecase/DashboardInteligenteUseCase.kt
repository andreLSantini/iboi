package com.iboi.ia.usecase

import com.iboi.ia.domain.PrioridadeAlerta
import com.iboi.ia.domain.StatusAlerta
import com.iboi.ia.repository.AlertaRepository
import com.iboi.ia.service.CalculadoraScoreRiscoService
import com.iboi.ia.service.PredicaoPesoService
import com.iboi.ia.service.RecomendacoesIAService
import com.iboi.rebanho.domain.StatusAnimal
import com.iboi.rebanho.repository.AnimalRepository
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.util.*

@Component
class DashboardInteligenteUseCase(
        private val animalRepository: AnimalRepository,
        private val alertaRepository: AlertaRepository,
        private val calculadoraScore: CalculadoraScoreRiscoService,
        private val predicaoPeso: PredicaoPesoService,
        private val recomendacoesIA: RecomendacoesIAService
) {

    fun execute(farmId: UUID): DashboardInteligenteResponse {
        val animaisAtivos = animalRepository.findByFarmIdAndStatus(farmId, StatusAnimal.ATIVO)

        // 1. Calcular scores de risco
        val animaisComRisco = animaisAtivos.map { animal ->
            val (score, fatores) = calculadoraScore.calcular(animal)
            AnimalRiscoDto(
                    animalId = animal.id!!,
                    brinco = animal.brinco,
                    nome = animal.nome,
                    scoreRisco = score,
                    fatoresRisco = fatores,
                    nivel = when {
                        score >= 70 -> "CRÍTICO"
                        score >= 50 -> "ALTO"
                        score >= 30 -> "MÉDIO"
                        else -> "BAIXO"
                    }
            )
        }.sortedByDescending { it.scoreRisco }

        // 2. Gerar predições de peso
        val predicoesPeso = animaisAtivos.take(5).mapNotNull { animal ->
            predicaoPeso.predizer(animal, 90)?.let {
                PredicaoPesoDto(
                        animalId = animal.id!!,
                        brinco = animal.brinco,
                        pesoAtual = animal.pesoAtual,
                        pesoPrevistoEm90Dias = it
                )
            }
        }

        // 3. Alertas críticos
        val alertasCriticos = alertaRepository.countByFarmIdAndStatusAndPrioridade(
                farmId, StatusAlerta.ATIVO, PrioridadeAlerta.CRITICA
        )

        // 4. Recomendações IA
        val recomendacoes = animaisComRisco.take(3).flatMap { animalRisco ->
            val animal = animaisAtivos.find { it.id == animalRisco.animalId }!!
            recomendacoesIA.gerar(animal, animalRisco.scoreRisco)
                    .map { RecomendacaoDto("Animal ${animalRisco.brinco}", it) }
        }

        return DashboardInteligenteResponse(
                animaisRiscoAlto = animaisComRisco.filter { it.scoreRisco >= 50 },
                predicoesPeso = predicoesPeso,
                alertasCriticos = alertasCriticos.toInt(),
                recomendacoesIA = recomendacoes,
                scoreRiscoMedio = animaisComRisco.map { it.scoreRisco }.average().toInt()
        )
    }
}

data class DashboardInteligenteResponse(
        val animaisRiscoAlto: List<AnimalRiscoDto>,
        val predicoesPeso: List<PredicaoPesoDto>,
        val alertasCriticos: Int,
        val recomendacoesIA: List<RecomendacaoDto>,
        val scoreRiscoMedio: Int
)

data class AnimalRiscoDto(
        val animalId: UUID,
        val brinco: String,
        val nome: String?,
        val scoreRisco: Int,
        val fatoresRisco: String,
        val nivel: String
)

data class PredicaoPesoDto(
        val animalId: UUID,
        val brinco: String,
        val pesoAtual: BigDecimal?,
        val pesoPrevistoEm90Dias: BigDecimal
)

data class RecomendacaoDto(
        val contexto: String,
        val recomendacao: String
)
