package com.iboi.rebanho.usecase

import com.iboi.rebanho.api.dto.AnimalDto
import com.iboi.rebanho.api.dto.FiltrarAnimaisRequest
import com.iboi.rebanho.api.dto.toDto
import com.iboi.rebanho.repository.AnimalRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class ListarAnimaisUseCase(
        private val animalRepository: AnimalRepository
) {

    fun execute(farmId: UUID, filtro: FiltrarAnimaisRequest?, pageable: Pageable): Page<AnimalDto> {
        return animalRepository.findByFarmIdWithFilters(
                farmId = farmId,
                status = filtro?.status,
                categoria = filtro?.categoria,
                loteId = filtro?.loteId,
                sexo = filtro?.sexo,
                pageable = pageable
        ).map { it.toDto() }
    }
}
