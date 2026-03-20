package com.iboi.rebanho.usecase

import com.iboi.rebanho.api.dto.AnimalDto
import com.iboi.rebanho.api.dto.AnimalResumoDto
import com.iboi.rebanho.api.dto.FiltrarAnimaisRequest
import com.iboi.rebanho.api.dto.LoteResumoDto
import com.iboi.rebanho.domain.Animal
import com.iboi.rebanho.repository.AnimalRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import java.time.Period
import java.util.*

@Component
class ListarAnimaisUseCase(
        private val animalRepository: AnimalRepository
) {

    fun execute(farmId: UUID, filtro: FiltrarAnimaisRequest?, pageable: Pageable): Page<AnimalDto> {
        val animais = animalRepository.findByFarmIdWithFilters(
                farmId = farmId,
                status = filtro?.status,
                categoria = filtro?.categoria,
                loteId = filtro?.loteId,
                sexo = filtro?.sexo,
                pageable = pageable
        )

        return animais.map { toDto(it) }
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
                    LoteResumoDto(it.id!!, it.nome)
                },
                pai = animal.pai?.let {
                    AnimalResumoDto(it.id!!, it.brinco, it.nome)
                },
                mae = animal.mae?.let {
                    AnimalResumoDto(it.id!!, it.brinco, it.nome)
                },
                observacoes = animal.observacoes
        )
    }
}
