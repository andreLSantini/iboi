package com.iboi.rebanho.usecase

import com.iboi.identity.infrastructure.repository.FarmRepository
import com.iboi.identity.infrastructure.repository.PastureRepository
import com.iboi.identity.infrastructure.repository.UsuarioRepository
import com.iboi.rebanho.api.dto.MovimentacaoAnimalDto
import com.iboi.rebanho.api.dto.RegistrarMovimentacaoAnimalRequest
import com.iboi.rebanho.api.dto.toDto
import com.iboi.rebanho.domain.Evento
import com.iboi.rebanho.domain.MovimentacaoAnimal
import com.iboi.rebanho.domain.StatusAnimal
import com.iboi.rebanho.domain.TipoEvento
import com.iboi.rebanho.domain.TipoMovimentacaoAnimal
import com.iboi.rebanho.repository.AnimalRepository
import com.iboi.rebanho.repository.EventoRepository
import com.iboi.rebanho.repository.MovimentacaoAnimalRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Component
class RegistrarMovimentacaoAnimalUseCase(
        private val animalRepository: AnimalRepository,
        private val farmRepository: FarmRepository,
        private val pastureRepository: PastureRepository,
        private val usuarioRepository: UsuarioRepository,
        private val movimentacaoAnimalRepository: MovimentacaoAnimalRepository,
        private val eventoRepository: EventoRepository
) {

    @Transactional
    fun execute(farmId: UUID, emailUsuario: String, animalId: UUID, request: RegistrarMovimentacaoAnimalRequest): MovimentacaoAnimalDto {
        val animal = animalRepository.findById(animalId).orElseThrow {
            IllegalArgumentException("Animal nao encontrado")
        }

        if (animal.farm.id != farmId) {
            throw IllegalArgumentException("Animal nao pertence a fazenda ativa")
        }

        val responsavel = usuarioRepository.findByEmail(emailUsuario)
        val origemFarm = animal.farm
        val origemPasture = animal.pasture

        val destinoFarm = request.destinoFarmId?.let {
            farmRepository.findById(it).orElseThrow {
                IllegalArgumentException("Fazenda de destino nao encontrada")
            }
        }

        if (destinoFarm != null && destinoFarm.empresa.id != origemFarm.empresa.id) {
            throw IllegalArgumentException("Transferencias entre empresas diferentes nao sao permitidas")
        }

        val destinoPasture = request.destinoPastureId?.let {
            pastureRepository.findById(it).orElseThrow {
                IllegalArgumentException("Pasto de destino nao encontrado")
            }
        }

        when (request.tipo) {
            TipoMovimentacaoAnimal.ENTRE_PASTOS -> {
                require(destinoPasture != null) { "Movimentacao entre pastos exige pasto de destino" }
                if (destinoPasture.farm.id != farmId) {
                    throw IllegalArgumentException("Pasto de destino nao pertence a fazenda ativa")
                }
                animal.pasture = destinoPasture
            }

            TipoMovimentacaoAnimal.ENTRE_FAZENDAS -> {
                require(destinoFarm != null) { "Transferencia entre fazendas exige fazenda de destino" }
                if (destinoPasture != null && destinoPasture.farm.id != destinoFarm.id) {
                    throw IllegalArgumentException("Pasto de destino nao pertence a fazenda de destino")
                }
                animal.farm = destinoFarm
                animal.pasture = destinoPasture
                animal.lote = null
                animal.status = StatusAnimal.ATIVO
            }

            TipoMovimentacaoAnimal.SAIDA_EXTERNA -> {
                animal.status = StatusAnimal.TRANSFERIDO
                animal.pasture = null
                animal.lote = null
            }

            TipoMovimentacaoAnimal.ENTRADA_EXTERNA -> {
                if (destinoPasture != null && destinoPasture.farm.id != farmId) {
                    throw IllegalArgumentException("Pasto de destino nao pertence a fazenda ativa")
                }
                animal.status = StatusAnimal.ATIVO
                animal.pasture = destinoPasture
            }
        }

        animal.atualizadoEm = LocalDateTime.now()
        animalRepository.save(animal)

        val movimentacao = movimentacaoAnimalRepository.save(
                MovimentacaoAnimal(
                        animal = animal,
                        tipo = request.tipo,
                        farmOrigem = origemFarm,
                        farmDestino = when (request.tipo) {
                            TipoMovimentacaoAnimal.ENTRE_FAZENDAS -> destinoFarm
                            TipoMovimentacaoAnimal.ENTRADA_EXTERNA -> origemFarm
                            else -> null
                        },
                        pastureOrigem = origemPasture,
                        pastureDestino = destinoPasture,
                        movimentadaEm = request.movimentadaEm,
                        numeroGta = request.numeroGta,
                        documentoExterno = request.documentoExterno,
                        motivo = request.motivo,
                        observacoes = request.observacoes,
                        responsavel = responsavel
                )
        )

        eventoRepository.save(
                Evento(
                        animal = animal,
                        farm = origemFarm,
                        tipo = TipoEvento.MOVIMENTACAO,
                        data = request.movimentadaEm,
                        descricao = buildDescricaoMovimentacao(request, origemFarm.name, destinoFarm?.name, origemPasture?.name, destinoPasture?.name),
                        responsavel = responsavel
                )
        )

        return movimentacao.toDto()
    }

    private fun buildDescricaoMovimentacao(
            request: RegistrarMovimentacaoAnimalRequest,
            origemFarmNome: String,
            destinoFarmNome: String?,
            origemPastoNome: String?,
            destinoPastoNome: String?
    ): String = when (request.tipo) {
        TipoMovimentacaoAnimal.ENTRE_PASTOS ->
            "Movimentado do pasto ${origemPastoNome ?: "nao informado"} para ${destinoPastoNome ?: "nao informado"}"
        TipoMovimentacaoAnimal.ENTRE_FAZENDAS ->
            "Transferido da fazenda $origemFarmNome para ${destinoFarmNome ?: "nao informada"}"
        TipoMovimentacaoAnimal.SAIDA_EXTERNA ->
            "Saida externa registrada${request.numeroGta?.let { " com GTA $it" } ?: ""}"
        TipoMovimentacaoAnimal.ENTRADA_EXTERNA ->
            "Entrada externa registrada${request.numeroGta?.let { " com GTA $it" } ?: ""}"
    }
}
