package com.iboi.rebanho.usecase

import com.iboi.rebanho.api.dto.AtualizarLoteRequest
import com.iboi.rebanho.api.dto.LoteDto
import com.iboi.rebanho.api.exception.AcessoNegadoException
import com.iboi.rebanho.api.exception.DadosInvalidosException
import com.iboi.rebanho.api.exception.LoteNaoEncontradoException
import com.iboi.rebanho.domain.Lote
import com.iboi.rebanho.repository.AnimalRepository
import com.iboi.rebanho.repository.LoteRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Component
class AtualizarLoteUseCase(
        private val loteRepository: LoteRepository,
        private val animalRepository: AnimalRepository
) {

    @Transactional
    fun execute(loteId: UUID, farmId: UUID, request: AtualizarLoteRequest): LoteDto {
        val lote = loteRepository.findById(loteId)
                .orElseThrow { LoteNaoEncontradoException("Lote com ID $loteId não encontrado") }

        if (lote.farm.id != farmId) {
            throw AcessoNegadoException("Você não tem permissão para atualizar este lote")
        }

        // Verificar nome duplicado se for alterar o nome
        if (request.nome != null && request.nome != lote.nome) {
            if (loteRepository.existsByNomeAndFarmId(request.nome, farmId)) {
                throw DadosInvalidosException("Já existe um lote com o nome '${request.nome}' nesta fazenda")
            }
            lote.nome = request.nome
        }

        request.descricao?.let { lote.descricao = it }
        request.ativo?.let { lote.ativo = it }

        val atualizado = loteRepository.save(lote)
        return toDto(atualizado)
    }

    private fun toDto(lote: Lote): LoteDto {
        val quantidade = animalRepository.findByLoteId(lote.id!!).size

        return LoteDto(
                id = lote.id!!,
                nome = lote.nome,
                descricao = lote.descricao,
                ativo = lote.ativo,
                quantidadeAnimais = quantidade,
                criadoEm = lote.criadoEm
        )
    }
}
