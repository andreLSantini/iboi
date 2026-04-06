package com.iboi.rebanho.usecase

import com.iboi.identity.infrastructure.repository.FarmRepository
import com.iboi.identity.infrastructure.repository.PastureRepository
import com.iboi.rebanho.api.dto.AnimalDto
import com.iboi.rebanho.api.dto.CadastrarAnimalRequest
import com.iboi.rebanho.api.dto.toDto
import com.iboi.rebanho.domain.Animal
import com.iboi.rebanho.domain.StatusAnimal
import com.iboi.rebanho.repository.AnimalRepository
import com.iboi.rebanho.repository.LoteRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Component
class CadastrarAnimalUseCase(
        private val animalRepository: AnimalRepository,
        private val farmRepository: FarmRepository,
        private val loteRepository: LoteRepository,
        private val pastureRepository: PastureRepository
) {

    @Transactional
    fun execute(farmId: UUID, request: CadastrarAnimalRequest): AnimalDto {
        val farm = farmRepository.findById(farmId).orElseThrow {
            IllegalArgumentException("Fazenda nao encontrada")
        }

        if (animalRepository.existsByBrincoAndFarmId(request.brinco, farmId)) {
            throw IllegalArgumentException("Ja existe um animal com o brinco ${request.brinco} nesta fazenda")
        }

        if (!request.rfid.isNullOrBlank() && animalRepository.existsByRfidAndFarmId(request.rfid, farmId)) {
            throw IllegalArgumentException("Ja existe um animal com este RFID nesta fazenda")
        }

        if (!request.codigoSisbov.isNullOrBlank() && animalRepository.existsByCodigoSisbov(request.codigoSisbov)) {
            throw IllegalArgumentException("Ja existe um animal com este codigo SISBOV")
        }

        val lote = request.loteId?.let {
            loteRepository.findById(it).orElseThrow {
                IllegalArgumentException("Lote nao encontrado")
            }.also { loteEncontrado ->
                if (loteEncontrado.farm.id != farmId) {
                    throw IllegalArgumentException("Lote nao pertence a fazenda informada")
                }
            }
        }

        val pasture = request.pastureId?.let {
            pastureRepository.findById(it).orElseThrow {
                IllegalArgumentException("Pasto nao encontrado")
            }.also { pastureEncontrado ->
                if (pastureEncontrado.farm.id != farmId) {
                    throw IllegalArgumentException("Pasto nao pertence a fazenda informada")
                }
            }
        }

        val pai = request.paiId?.let {
            animalRepository.findById(it).orElseThrow {
                IllegalArgumentException("Animal pai nao encontrado")
            }.also { paiEncontrado ->
                if (paiEncontrado.farm.id != farmId) {
                    throw IllegalArgumentException("Animal pai nao pertence a fazenda informada")
                }
            }
        }

        val mae = request.maeId?.let {
            animalRepository.findById(it).orElseThrow {
                IllegalArgumentException("Animal mae nao encontrado")
            }.also { maeEncontrada ->
                if (maeEncontrada.farm.id != farmId) {
                    throw IllegalArgumentException("Animal mae nao pertence a fazenda informada")
                }
            }
        }

        val animal = animalRepository.save(
                Animal(
                        brinco = request.brinco,
                        rfid = request.rfid,
                        codigoSisbov = request.codigoSisbov,
                        nome = request.nome,
                        sexo = request.sexo,
                        raca = request.raca,
                        dataNascimento = request.dataNascimento,
                        pesoAtual = request.pesoAtual,
                        categoria = request.categoria,
                        origem = request.origem,
                        farm = farm,
                        lote = lote,
                        pasture = pasture,
                        pai = pai,
                        mae = mae,
                        observacoes = request.observacoes,
                        dataEntrada = request.dataEntrada ?: request.dataNascimento,
                        sisbovAtivo = request.sisbovAtivo,
                        status = StatusAnimal.ATIVO
                )
        )

        return animal.toDto()
    }
}
