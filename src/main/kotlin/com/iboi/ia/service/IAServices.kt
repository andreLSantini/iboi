package com.iboi.ia.service

import com.iboi.rebanho.domain.Animal
import com.iboi.rebanho.domain.TipoEvento
import com.iboi.rebanho.repository.EventoRepository
import com.iboi.sanitario.domain.StatusAgendamento
import com.iboi.sanitario.repository.AgendamentoSanitarioRepository
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.Period
import java.time.temporal.ChronoUnit

@Service
class CalculadoraScoreRiscoService(
        private val agendamentoRepository: AgendamentoSanitarioRepository,
        private val eventoRepository: EventoRepository
) {

    fun calcular(animal: Animal): Pair<Int, String> {
        var score = 0
        val fatores = mutableListOf<String>()

        // 1. Vacinas atrasadas (+30 pontos)
        val vacinasAtrasadas = agendamentoRepository.findByAnimalIdAndStatus(
                animal.id!!, StatusAgendamento.PENDENTE
        ).filter { it.dataPrevista.isBefore(LocalDate.now()) }

        if (vacinasAtrasadas.isNotEmpty()) {
            score += 30
            fatores.add("${vacinasAtrasadas.size} vacinas atrasadas")
        }

        // 2. Histórico de doenças/tratamentos (+20 pontos)
        val tratamentosRecentes = eventoRepository.findByAnimalIdAndTipo(animal.id, TipoEvento.TRATAMENTO)
                .filter { ChronoUnit.DAYS.between(it.data, LocalDate.now()) <= 90 }

        if (tratamentosRecentes.size >= 3) {
            score += 20
            fatores.add("3+ tratamentos nos últimos 90 dias")
        }

        // 3. Idade avançada (+15 pontos se > 8 anos)
        val idadeAnos = Period.between(animal.dataNascimento, LocalDate.now()).years
        if (idadeAnos > 8) {
            score += 15
            fatores.add("Idade avançada ($idadeAnos anos)")
        }

        // 4. Peso muito baixo (+25 pontos)
        animal.pesoAtual?.let { peso ->
            if (peso < BigDecimal(200)) {
                score += 25
                fatores.add("Peso crítico (${peso}kg)")
            }
        }

        // 5. Sem pesagem recente (+10 pontos)
        val ultimaPesagem = eventoRepository.findByAnimalIdAndTipo(animal.id, TipoEvento.PESAGEM)
                .firstOrNull()

        if (ultimaPesagem == null || ChronoUnit.DAYS.between(ultimaPesagem.data, LocalDate.now()) > 90) {
            score += 10
            fatores.add("Sem pesagem há mais de 90 dias")
        }

        return Pair(score.coerceAtMost(100), fatores.joinToString(", "))
    }
}

@Service
class PredicaoPesoService(
        private val eventoRepository: EventoRepository
) {

    fun predizer(animal: Animal, diasFuturos: Int): BigDecimal? {
        val pesagens = eventoRepository.findByAnimalIdAndTipo(animal.id!!, TipoEvento.PESAGEM)
                .filter { it.peso != null }
                .sortedBy { it.data }

        if (pesagens.size < 2) return null

        // Calcular ganho médio de peso por dia (regressão linear simples)
        val primeira = pesagens.first()
        val ultima = pesagens.last()

        val diasEntre = ChronoUnit.DAYS.between(primeira.data, ultima.data)
        if (diasEntre == 0L) return null

        val ganhoPeso = ultima.peso!!.subtract(primeira.peso!!)
        val ganhoPorDia = ganhoPeso.divide(BigDecimal(diasEntre), 4, RoundingMode.HALF_UP)

        val pesoFuturo = ultima.peso!!.add(ganhoPorDia.multiply(BigDecimal(diasFuturos)))

        return pesoFuturo.setScale(2, RoundingMode.HALF_UP)
    }
}

@Service
class RecomendacoesIAService {

    fun gerar(animal: Animal, scoreRisco: Int): List<String> {
        val recomendacoes = mutableListOf<String>()

        // Baseado no score de risco
        when {
            scoreRisco > 70 -> {
                recomendacoes.add("⚠️ URGENTE: Avaliação veterinária imediata recomendada")
                recomendacoes.add("Isolar animal para monitoramento intensivo")
            }
            scoreRisco > 50 -> {
                recomendacoes.add("Agendar consulta veterinária preventiva")
                recomendacoes.add("Reforçar protocolo sanitário")
            }
            scoreRisco > 30 -> {
                recomendacoes.add("Monitorar de perto nos próximos 15 dias")
            }
        }

        // Baseado em idade
        val idadeAnos = Period.between(animal.dataNascimento, LocalDate.now()).years
        when {
            idadeAnos >= 8 -> recomendacoes.add("Considerar descarte/venda (idade avançada)")
            idadeAnos in 2..3 && animal.sexo == com.iboi.rebanho.domain.Sexo.MACHO -> {
                recomendacoes.add("Idade ideal para venda (boi gordo)")
            }
        }

        // Baseado em peso
        animal.pesoAtual?.let { peso ->
            when {
                peso < BigDecimal(250) -> recomendacoes.add("Melhorar nutrição urgentemente")
                peso > BigDecimal(500) -> recomendacoes.add("Peso excelente para venda")
                else -> {} // Peso normal, nenhuma recomendação adicional
            }
        }

        return recomendacoes
    }
}
