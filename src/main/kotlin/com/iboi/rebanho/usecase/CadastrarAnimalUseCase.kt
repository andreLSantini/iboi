package com.iboi.rebanho.usecase

import com.iboi.identity.infrastructure.repository.FarmRepository
import com.iboi.rebanho.api.dto.AnimalDto
import com.iboi.rebanho.api.dto.CadastrarAnimalRequest
import com.iboi.rebanho.domain.Animal
import com.iboi.rebanho.domain.StatusAnimal
import com.iboi.rebanho.repository.AnimalRepository
import com.iboi.rebanho.repository.LoteRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.Period
import java.util.*

@Component
class CadastrarAnimalUseCase(
        private val animalRepository: AnimalRepository,
        private val farmRepository: FarmRepository,
        private val loteRepository: LoteRepository
) {

    @Transactional
    fun execute(farmId: UUID, request: CadastrarAnimalRequest): AnimalDto {

        // Validações
        val farm = farmRepository.findById(farmId).orElseThrow {
            IllegalArgumentException("Fazenda não encontrada")
        }

        if (animalRepository.existsByBrincoAndFarmId(request.brinco, farmId)) {
            throw IllegalArgumentException("Já existe um animal com o brinco ${request.brinco} nesta fazenda")
        }

        val lote = request.loteId?.let {
            loteRepository.findById(it).orElseThrow {
                IllegalArgumentException("Lote não encontrado")
            }
        }

        val pai = request.paiId?.let {
            animalRepository.findById(it).orElseThrow {
                IllegalArgumentException("Animal pai não encontrado")
            }
        }

        val mae = request.maeId?.let {
            animalRepository.findById(it).orElseThrow {
                IllegalArgumentException("Animal mãe não encontrado")
            }
        }

        // Criar animal
        val animal = animalRepository.save(
                Animal(
                        brinco = request.brinco,
                        nome = request.nome,
                        sexo = request.sexo,
                        raca = request.raca,
                        dataNascimento = request.dataNascimento,
                        pesoAtual = request.pesoAtual,
                        categoria = request.categoria,
                        farm = farm,
                        lote = lote,
                        pai = pai,
                        mae = mae,
                        observacoes = request.observacoes,
                        status = StatusAnimal.ATIVO
                )
        )

        return toDto(animal)
    }

    private fun toDto(animal: Animal): AnimalDto {
        val idade = Period.between(animal.dataNascimento, java.time.LocalDate.now()).toTotalMonths().toInt()

        return AnimalDto(
                id = animal.id!!,
                brinco = animal.brinco,
                nome = animal.nome,
                sexo = animal.sexo,
                raca = animal.raca,
                dataNascimento = animal.dataNascimento,
                idade = idade,
                pesoAtual = animal.pesoAtual,
                status = animal.status,
                categoria = animal.categoria,
                lote = animal.lote?.let {
                    com.iboi.rebanho.api.dto.LoteResumoDto(it.id!!, it.nome)
                },
                pai = animal.pai?.let {
                    com.iboi.rebanho.api.dto.AnimalResumoDto(it.id!!, it.brinco, it.nome)
                },
                mae = animal.mae?.let {
                    com.iboi.rebanho.api.dto.AnimalResumoDto(it.id!!, it.brinco, it.nome)
                },
                observacoes = animal.observacoes
        )
    }
}
