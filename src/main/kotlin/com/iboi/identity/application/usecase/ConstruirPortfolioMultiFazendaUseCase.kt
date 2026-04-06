package com.iboi.identity.application.usecase

import com.iboi.identity.api.dto.response.AreaOperacionalDto
import com.iboi.identity.api.dto.response.FarmOperacionalDto
import com.iboi.identity.api.dto.response.MultiFarmPortfolioDto
import com.iboi.identity.api.dto.response.MultiFarmResumoDto
import com.iboi.identity.infrastructure.repository.FarmRepository
import com.iboi.identity.infrastructure.repository.PastureRepository
import com.iboi.identity.infrastructure.repository.UserFarmProfileRepository
import com.iboi.rebanho.domain.StatusAnimal
import com.iboi.rebanho.repository.AnimalRepository
import com.iboi.rebanho.repository.LoteRepository
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class ConstruirPortfolioMultiFazendaUseCase(
        private val userFarmProfileRepository: UserFarmProfileRepository,
        private val farmRepository: FarmRepository,
        private val pastureRepository: PastureRepository,
        private val animalRepository: AnimalRepository,
        private val loteRepository: LoteRepository
) {

    fun execute(userId: UUID, empresaId: UUID): MultiFarmPortfolioDto {
        val accessibleFarmIds = userFarmProfileRepository.findAllByUsuario_Id(userId)
                .map { it.farm.id }
                .toSet()

        val fazendas = farmRepository.findByEmpresa_Id(empresaId)
                .filter { accessibleFarmIds.contains(it.id) }
                .sortedBy { it.name.lowercase() }

        val fazendasDto = fazendas.map { farm ->
            val pastos = pastureRepository.findByFarmIdOrderByNameAsc(farm.id!!)
            val animaisAtivos = animalRepository.findByFarmIdAndStatus(farm.id, StatusAnimal.ATIVO)
            val lotesAtivos = loteRepository.findByFarmIdAndAtivo(farm.id, true)
            val contagemPorPasto = animaisAtivos.groupingBy { it.pasture?.id }.eachCount()
            val totalAreaPastos = pastos.sumOf { it.areaHa ?: 0.0 }

            FarmOperacionalDto(
                    id = farm.id,
                    nome = farm.name,
                    cidade = farm.city,
                    estado = farm.state,
                    tipoProducao = farm.productionType.name,
                    ativa = farm.active,
                    areaTotalHa = farm.size,
                    areaPastagemHa = farm.grazingArea,
                    areaProdutivaHa = farm.productiveArea,
                    capacidadeEstimada = farm.estimatedCapacity,
                    animaisAtivos = animaisAtivos.size.toLong(),
                    lotesAtivos = lotesAtivos.size,
                    totalPastos = pastos.size,
                    totalAreaPastosHa = totalAreaPastos,
                    taxaOcupacaoEstimada = farm.estimatedCapacity
                            ?.takeIf { it > 0 }
                            ?.let { capacidade -> (animaisAtivos.size.toDouble() / capacidade.toDouble()) * 100.0 },
                    areasOperacionais = pastos.map { pasto ->
                        AreaOperacionalDto(
                                id = pasto.id!!,
                                nome = pasto.name,
                                areaHa = pasto.areaHa,
                                ativa = pasto.active,
                                animaisAtivos = contagemPorPasto[pasto.id] ?: 0
                        )
                    }
            )
        }

        return MultiFarmPortfolioDto(
                resumo = MultiFarmResumoDto(
                        totalFazendas = fazendasDto.size,
                        fazendasAtivas = fazendasDto.count { it.ativa },
                        totalAnimaisAtivos = fazendasDto.sumOf { it.animaisAtivos },
                        totalLotesAtivos = fazendasDto.sumOf { it.lotesAtivos },
                        totalPastos = fazendasDto.sumOf { it.totalPastos },
                        areaTotalHa = fazendasDto.sumOf { it.areaTotalHa ?: 0.0 },
                        areaPastagemHa = fazendasDto.sumOf { it.areaPastagemHa ?: 0.0 },
                        areaProdutivaHa = fazendasDto.sumOf { it.areaProdutivaHa ?: 0.0 }
                ),
                fazendas = fazendasDto
        )
    }
}
