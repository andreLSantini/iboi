package com.iboi.financeiro.usecase

import com.iboi.financeiro.api.dto.ReceitaDto
import com.iboi.financeiro.api.dto.RegistrarReceitaRequest
import com.iboi.financeiro.domain.Receita
import com.iboi.financeiro.repository.ReceitaRepository
import com.iboi.identity.infrastructure.repository.FarmRepository
import com.iboi.identity.infrastructure.repository.UsuarioRepository
import com.iboi.rebanho.repository.AnimalRepository
import com.iboi.rebanho.repository.LoteRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Component
class RegistrarReceitaUseCase(
        private val receitaRepository: ReceitaRepository,
        private val farmRepository: FarmRepository,
        private val usuarioRepository: UsuarioRepository,
        private val animalRepository: AnimalRepository,
        private val loteRepository: LoteRepository
) {

    @Transactional
    fun execute(farmId: UUID, emailUsuario: String, request: RegistrarReceitaRequest): ReceitaDto {
        val farm = farmRepository.findById(farmId).orElseThrow {
            IllegalArgumentException("Fazenda nao encontrada")
        }

        val animal = request.animalId?.let { animalId ->
            animalRepository.findById(animalId).orElseThrow {
                IllegalArgumentException("Animal nao encontrado")
            }.also {
                if (it.farm.id != farmId) {
                    throw IllegalArgumentException("Animal nao pertence a fazenda informada")
                }
            }
        }

        val lote = request.loteId?.let { loteId ->
            loteRepository.findById(loteId).orElseThrow {
                IllegalArgumentException("Lote nao encontrado")
            }.also {
                if (it.farm.id != farmId) {
                    throw IllegalArgumentException("Lote nao pertence a fazenda informada")
                }
            }
        }

        val responsavel = usuarioRepository.findByEmail(emailUsuario)
        val receita = receitaRepository.save(
                Receita(
                        farm = farm,
                        tipo = request.tipo,
                        descricao = request.descricao,
                        valor = request.valor,
                        data = request.data,
                        formaPagamento = request.formaPagamento,
                        lote = lote,
                        animal = animal,
                        responsavel = responsavel,
                        comprador = request.comprador,
                        quantidadeAnimais = request.quantidadeAnimais,
                        observacoes = request.observacoes
                )
        )

        return ReceitaDto(
                id = receita.id!!,
                tipo = receita.tipo,
                descricao = receita.descricao,
                valor = receita.valor,
                data = receita.data,
                formaPagamento = receita.formaPagamento,
                comprador = receita.comprador,
                quantidadeAnimais = receita.quantidadeAnimais,
                responsavel = receita.responsavel?.nome,
                observacoes = receita.observacoes
        )
    }
}
