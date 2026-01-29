package com.iboi.rebanho.usecase

import com.iboi.rebanho.api.dto.AnimalDto
import com.iboi.rebanho.api.dto.AnimalResumoDto
import com.iboi.rebanho.api.dto.FiltrarAnimaisRequest
import com.iboi.rebanho.api.dto.LoteResumoDto
import com.iboi.rebanho.domain.Animal
import com.iboi.rebanho.repository.AnimalRepository
import org.springframework.stereotype.Component
import java.time.Period
import java.util.*

@Component
class ListarAnimaisUseCase(
        private val animalRepository: AnimalRepository
) {

    fun execute(farmId: UUID, filtro: FiltrarAnimaisRequest?): List<AnimalDto> {
        var animais = animalRepository.findByFarmId(farmId)

        // Aplicar filtros
        filtro?.let {
            if (it.status != null) {
                animais = animais.filter { animal -> animal.status == it.status }
            }
            if (it.categoria != null) {
                animais = animais.filter { animal -> animal.categoria == it.categoria }
            }
            if (it.loteId != null) {
                animais = animais.filter { animal -> animal.lote?.id == it.loteId }
            }
            if (it.sexo != null) {
                animais = animais.filter { animal -> animal.sexo == it.sexo }
            }
        }

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
