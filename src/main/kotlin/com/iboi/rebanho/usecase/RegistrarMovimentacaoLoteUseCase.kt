package com.iboi.rebanho.usecase

import com.iboi.rebanho.api.dto.AnimalResumoDto
import com.iboi.rebanho.api.dto.FarmResumoDto
import com.iboi.rebanho.api.dto.LoteResumoDto
import com.iboi.rebanho.api.dto.MovimentacaoLoteResultadoDto
import com.iboi.rebanho.api.dto.PastureResumoDto
import com.iboi.rebanho.api.dto.RegistrarMovimentacaoAnimalRequest
import com.iboi.rebanho.api.dto.RegistrarMovimentacaoLoteRequest
import com.iboi.rebanho.domain.StatusAnimal
import com.iboi.rebanho.repository.AnimalRepository
import com.iboi.rebanho.repository.LoteRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Component
class RegistrarMovimentacaoLoteUseCase(
        private val animalRepository: AnimalRepository,
        private val loteRepository: LoteRepository,
        private val registrarMovimentacaoAnimalUseCase: RegistrarMovimentacaoAnimalUseCase
) {

    @Transactional
    fun execute(farmId: UUID, emailUsuario: String, request: RegistrarMovimentacaoLoteRequest): MovimentacaoLoteResultadoDto {
        val animais = animalRepository.findAllById(request.animalIds)
                .toList()
                .distinctBy { it.id }

        if (animais.isEmpty()) {
            throw IllegalArgumentException("Nenhum animal encontrado para movimentacao")
        }

        if (animais.size != request.animalIds.distinct().size) {
            throw IllegalArgumentException("Um ou mais animais informados nao foram encontrados")
        }

        if (animais.any { it.farm.id != farmId }) {
            throw IllegalArgumentException("Todos os animais devem pertencer a fazenda ativa")
        }

        if (animais.any { it.status != StatusAnimal.ATIVO }) {
            throw IllegalArgumentException("A movimentacao em lote aceita apenas animais ativos")
        }

        val loteOrigem = request.loteOrigemId?.let { loteId ->
            loteRepository.findById(loteId).orElseThrow {
                IllegalArgumentException("Lote de origem nao encontrado")
            }.also { lote ->
                if (lote.farm.id != farmId) {
                    throw IllegalArgumentException("Lote de origem nao pertence a fazenda ativa")
                }
            }
        }

        if (loteOrigem != null && animais.any { it.lote?.id != loteOrigem.id }) {
            throw IllegalArgumentException("Todos os animais selecionados devem pertencer ao lote de origem")
        }

        val requestBase = RegistrarMovimentacaoAnimalRequest(
                tipo = request.tipo,
                movimentadaEm = request.movimentadaEm,
                destinoFarmId = request.destinoFarmId,
                destinoPastureId = request.destinoPastureId,
                destinoLoteId = request.destinoLoteId,
                numeroGta = request.numeroGta,
                documentoExterno = request.documentoExterno,
                motivo = request.motivo,
                observacoes = request.observacoes
        )

        val movimentacoes = animais.map { animal ->
            registrarMovimentacaoAnimalUseCase.execute(
                    farmId = farmId,
                    emailUsuario = emailUsuario,
                    animalId = animal.id!!,
                    request = requestBase
            )
        }

        val loteDestino = animais.firstOrNull()?.let {
            animalRepository.findById(it.id!!).orElse(null)?.lote
        }
        val farmDestino = movimentacoes.firstOrNull()?.farmDestino
        val pastureDestino = movimentacoes.firstOrNull()?.pastureDestino

        return MovimentacaoLoteResultadoDto(
                totalAnimais = movimentacoes.size,
                tipo = request.tipo,
                movimentadaEm = request.movimentadaEm,
                loteOrigem = loteOrigem?.let { LoteResumoDto(it.id!!, it.nome) },
                loteDestino = loteDestino?.let { LoteResumoDto(it.id!!, it.nome) },
                farmDestino = farmDestino?.let { FarmResumoDto(it.id, it.nome) },
                pastureDestino = pastureDestino?.let { PastureResumoDto(it.id, it.nome) },
                animais = animais.map { AnimalResumoDto(it.id!!, it.brinco, it.nome) },
                movimentacoes = movimentacoes
        )
    }
}
