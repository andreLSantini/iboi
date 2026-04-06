package com.iboi.rebanho.usecase

import com.iboi.rebanho.api.dto.PesagemAnimalDto
import com.iboi.rebanho.api.dto.calcularGanhoMedioDiario
import com.iboi.rebanho.api.exception.AcessoNegadoException
import com.iboi.rebanho.api.exception.AnimalNaoEncontradoException
import com.iboi.rebanho.domain.TipoEvento
import com.iboi.rebanho.repository.AnimalRepository
import com.iboi.rebanho.repository.EventoRepository
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class ListarPesagensAnimalUseCase(
        private val animalRepository: AnimalRepository,
        private val eventoRepository: EventoRepository
) {

    fun execute(animalId: UUID, farmId: UUID): List<PesagemAnimalDto> {
        val animal = animalRepository.findById(animalId)
                .orElseThrow { AnimalNaoEncontradoException("Animal com ID $animalId nao encontrado") }

        if (animal.farm.id != farmId) {
            throw AcessoNegadoException("Voce nao tem permissao para acessar este animal")
        }

        val pesagens = eventoRepository.findByAnimalIdAndTipo(animalId, TipoEvento.PESAGEM)
                .filter { it.peso != null }
                .sortedBy { it.data }

        return pesagens.mapIndexed { index, evento ->
            val anterior = pesagens.getOrNull(index - 1)
            val variacaoPeso = anterior?.peso?.let { evento.peso!!.subtract(it) }
            val diasDesdeAnterior = anterior?.let { java.time.temporal.ChronoUnit.DAYS.between(it.data, evento.data) }

            PesagemAnimalDto(
                    id = evento.id!!,
                    data = evento.data,
                    peso = evento.peso!!,
                    variacaoPeso = variacaoPeso,
                    diasDesdeAnterior = diasDesdeAnterior,
                    ganhoMedioDiario = if (variacaoPeso != null && diasDesdeAnterior != null) {
                        calcularGanhoMedioDiario(variacaoPeso, diasDesdeAnterior)
                    } else null,
                    observacao = evento.descricao,
                    responsavel = evento.responsavel?.nome
            )
        }.sortedByDescending { it.data }
    }
}
