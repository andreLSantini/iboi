package com.iboi.rebanho.usecase

import com.iboi.identity.infrastructure.repository.PastureRepository
import com.iboi.rebanho.api.dto.AnimalDto
import com.iboi.rebanho.api.dto.AtualizarAnimalRequest
import com.iboi.rebanho.api.dto.toDto
import com.iboi.rebanho.api.exception.AcessoNegadoException
import com.iboi.rebanho.api.exception.AnimalNaoEncontradoException
import com.iboi.rebanho.api.exception.LoteNaoEncontradoException
import com.iboi.rebanho.repository.AnimalRepository
import com.iboi.rebanho.repository.LoteRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Component
class AtualizarAnimalUseCase(
        private val animalRepository: AnimalRepository,
        private val loteRepository: LoteRepository,
        private val pastureRepository: PastureRepository
) {

    @Transactional
    fun execute(animalId: UUID, farmId: UUID, request: AtualizarAnimalRequest): AnimalDto {
        val animal = animalRepository.findById(animalId)
                .orElseThrow { AnimalNaoEncontradoException("Animal com ID $animalId nao encontrado") }

        if (animal.farm.id != farmId) {
            throw AcessoNegadoException("Voce nao tem permissao para atualizar este animal")
        }

        if (!request.rfid.isNullOrBlank() && request.rfid != animal.rfid &&
                animalRepository.existsByRfidAndFarmId(request.rfid, farmId)
        ) {
            throw IllegalArgumentException("Ja existe um animal com este RFID nesta fazenda")
        }

        if (!request.codigoSisbov.isNullOrBlank() && request.codigoSisbov != animal.codigoSisbov &&
                animalRepository.existsByCodigoSisbov(request.codigoSisbov)
        ) {
            throw IllegalArgumentException("Ja existe um animal com este codigo SISBOV")
        }

        request.loteId?.let {
            val lote = loteRepository.findById(it)
                    .orElseThrow { LoteNaoEncontradoException("Lote nao encontrado") }

            if (lote.farm.id != farmId) {
                throw AcessoNegadoException("Lote nao pertence a sua fazenda")
            }

            animal.lote = lote
        }

        request.pastureId?.let {
            val pasture = pastureRepository.findById(it)
                    .orElseThrow { IllegalArgumentException("Pasto nao encontrado") }

            if (pasture.farm.id != farmId) {
                throw AcessoNegadoException("Pasto nao pertence a sua fazenda")
            }

            animal.pasture = pasture
        }

        request.rfid?.let { animal.rfid = it }
        request.codigoSisbov?.let { animal.codigoSisbov = it }
        request.nome?.let { animal.nome = it }
        request.raca?.let { animal.raca = it }
        request.pesoAtual?.let { animal.pesoAtual = it }
        request.categoria?.let { animal.categoria = it }
        request.status?.let { animal.status = it }
        request.dataEntrada?.let { animal.dataEntrada = it }
        request.sisbovAtivo?.let { animal.sisbovAtivo = it }
        request.observacoes?.let { animal.observacoes = it }

        animal.atualizadoEm = LocalDateTime.now()

        return animalRepository.save(animal).toDto()
    }
}
