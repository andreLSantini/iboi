package com.iboi.rebanho.usecase

import com.iboi.identity.infrastructure.repository.UsuarioRepository
import com.iboi.rebanho.api.dto.RegistrarVacinacaoAnimalRequest
import com.iboi.rebanho.api.dto.VacinacaoAnimalDto
import com.iboi.rebanho.api.dto.toDto
import com.iboi.rebanho.domain.Evento
import com.iboi.rebanho.domain.TipoEvento
import com.iboi.rebanho.domain.VacinacaoAnimal
import com.iboi.rebanho.repository.AnimalRepository
import com.iboi.rebanho.repository.EventoRepository
import com.iboi.rebanho.repository.VacinacaoAnimalRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.util.UUID

@Component
class RegistrarVacinacaoAnimalUseCase(
        private val animalRepository: AnimalRepository,
        private val usuarioRepository: UsuarioRepository,
        private val vacinacaoAnimalRepository: VacinacaoAnimalRepository,
        private val eventoRepository: EventoRepository
) {

    @Transactional
    fun execute(farmId: UUID, emailUsuario: String, animalId: UUID, request: RegistrarVacinacaoAnimalRequest): VacinacaoAnimalDto {
        val animal = animalRepository.findById(animalId).orElseThrow {
            IllegalArgumentException("Animal nao encontrado")
        }

        if (animal.farm.id != farmId) {
            throw IllegalArgumentException("Animal nao pertence a fazenda ativa")
        }

        val responsavel = usuarioRepository.findByEmail(emailUsuario)

        val vacinacao = vacinacaoAnimalRepository.save(
                VacinacaoAnimal(
                        animal = animal,
                        farm = animal.farm,
                        tipo = request.tipo,
                        nomeVacina = request.nomeVacina,
                        dose = request.dose,
                        unidadeMedida = request.unidadeMedida,
                        aplicadaEm = request.aplicadaEm,
                        proximaDoseEm = request.proximaDoseEm,
                        fabricante = request.fabricante,
                        loteVacina = request.loteVacina,
                        observacoes = request.observacoes,
                        responsavel = responsavel
                )
        )

        eventoRepository.save(
                Evento(
                        animal = animal,
                        farm = animal.farm,
                        tipo = TipoEvento.VACINA,
                        data = request.aplicadaEm,
                        descricao = "Vacina ${request.nomeVacina} aplicada",
                        produto = request.nomeVacina,
                        dose = request.dose ?: BigDecimal.ZERO,
                        unidadeMedida = request.unidadeMedida,
                        responsavel = responsavel
                )
        )

        return vacinacao.toDto()
    }
}
