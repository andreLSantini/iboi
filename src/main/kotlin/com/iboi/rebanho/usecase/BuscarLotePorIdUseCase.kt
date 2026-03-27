package com.iboi.rebanho.usecase

import com.iboi.rebanho.api.dto.LoteDto
import com.iboi.rebanho.api.exception.AcessoNegadoException
import com.iboi.rebanho.api.exception.LoteNaoEncontradoException
import com.iboi.rebanho.domain.Lote
import com.iboi.rebanho.repository.AnimalRepository
import com.iboi.rebanho.repository.LoteRepository
import org.springframework.stereotype.Component
import java.util.*

@Component
class BuscarLotePorIdUseCase(
        private val loteRepository: LoteRepository,
        private val animalRepository: AnimalRepository
) {

    fun execute(loteId: UUID, farmId: UUID): LoteDto {
        val lote = loteRepository.findById(loteId)
                .orElseThrow { LoteNaoEncontradoException("Lote com ID $loteId não encontrado") }

        if (lote.farm.id != farmId) {
            throw AcessoNegadoException("Você não tem permissão para acessar este lote")
        }

        return toDto(lote)
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
