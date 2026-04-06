package com.iboi.rebanho.usecase

import com.iboi.identity.infrastructure.repository.FarmRepository
import com.iboi.identity.infrastructure.repository.UsuarioRepository
import com.iboi.rebanho.api.dto.EventoDto
import com.iboi.rebanho.api.dto.RegistrarEventoRequest
import com.iboi.rebanho.api.dto.toDto
import com.iboi.rebanho.domain.Sexo
import com.iboi.rebanho.domain.Evento
import com.iboi.rebanho.domain.TipoEvento
import com.iboi.rebanho.repository.AnimalRepository
import com.iboi.rebanho.repository.EventoRepository
import com.iboi.rebanho.repository.LoteRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Component
class RegistrarEventoUseCase(
        private val eventoRepository: EventoRepository,
        private val animalRepository: AnimalRepository,
        private val farmRepository: FarmRepository,
        private val loteRepository: LoteRepository,
        private val usuarioRepository: UsuarioRepository
) {

    @Transactional
    fun execute(farmId: UUID, emailUsuario: String, request: RegistrarEventoRequest): EventoDto {

        val farm = farmRepository.findById(farmId).orElseThrow {
            IllegalArgumentException("Fazenda não encontrada")
        }

        val animal = animalRepository.findById(request.animalId).orElseThrow {
            IllegalArgumentException("Animal não encontrado")
        }

        if (animal.farm.id != farmId) {
            throw IllegalArgumentException("Animal não pertence a esta fazenda")
        }

        val loteDestino = request.loteDestinoId?.let {
            loteRepository.findById(it).orElseThrow {
                IllegalArgumentException("Lote destino não encontrado")
            }
        }

        val responsavel = usuarioRepository.findByEmail(emailUsuario)

        validarEventoReprodutivo(animal.sexo, request)

        val evento = eventoRepository.save(
                Evento(
                        animal = animal,
                        farm = farm,
                        tipo = request.tipo,
                        data = request.data,
                        descricao = request.descricao,
                        peso = request.peso,
                        produto = request.produto,
                        dose = request.dose,
                        unidadeMedida = request.unidadeMedida,
                        loteDestino = loteDestino,
                        valor = request.valor,
                        reprodutorNome = request.reprodutorNome,
                        protocoloReprodutivo = request.protocoloReprodutivo,
                        diagnosticoPositivo = request.diagnosticoPositivo,
                        dataPrevistaParto = request.dataPrevistaParto ?: dataPrevistaPartoAutomatica(request),
                        observacaoReprodutiva = request.observacaoReprodutiva,
                        responsavel = responsavel
                )
        )

        // Atualizar peso do animal se for evento de pesagem
        if (request.tipo == TipoEvento.PESAGEM && request.peso != null) {
            animal.pesoAtual = request.peso
            animalRepository.save(animal)
        }

        // Atualizar lote do animal se for movimentação
        if (request.tipo == TipoEvento.MOVIMENTACAO && loteDestino != null) {
            animal.lote = loteDestino
            animalRepository.save(animal)
        }

        return evento.toDto()
    }

    private fun validarEventoReprodutivo(sexo: Sexo, request: RegistrarEventoRequest) {
        val eventoReprodutivo = request.tipo in setOf(
                TipoEvento.INSEMINACAO,
                TipoEvento.COBERTURA,
                TipoEvento.DIAGNOSTICO_GESTACAO,
                TipoEvento.PARTO
        )

        if (!eventoReprodutivo) {
            return
        }

        if (sexo != Sexo.FEMEA) {
            throw IllegalArgumentException("Eventos reprodutivos desta etapa so podem ser registrados para femeas")
        }
    }

    private fun dataPrevistaPartoAutomatica(request: RegistrarEventoRequest) =
            if (request.tipo == TipoEvento.INSEMINACAO || request.tipo == TipoEvento.COBERTURA) {
                request.data.plusDays(285)
            } else {
                null
            }
}
