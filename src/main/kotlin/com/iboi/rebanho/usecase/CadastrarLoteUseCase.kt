package com.iboi.rebanho.usecase

import com.iboi.identity.infrastructure.repository.FarmRepository
import com.iboi.rebanho.api.dto.CadastrarLoteRequest
import com.iboi.rebanho.api.dto.LoteDto
import com.iboi.rebanho.api.exception.DadosInvalidosException
import com.iboi.rebanho.domain.Lote
import com.iboi.rebanho.repository.AnimalRepository
import com.iboi.rebanho.repository.LoteRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Component
class CadastrarLoteUseCase(
        private val loteRepository: LoteRepository,
        private val farmRepository: FarmRepository,
        private val animalRepository: AnimalRepository
) {

    @Transactional
    fun execute(farmId: UUID, request: CadastrarLoteRequest): LoteDto {
        val farm = farmRepository.findById(farmId)
                .orElseThrow { IllegalArgumentException("Fazenda não encontrada") }

        // Verificar se já existe lote com mesmo nome
        if (loteRepository.existsByNomeAndFarmId(request.nome, farmId)) {
            throw DadosInvalidosException("Já existe um lote com o nome '${request.nome}' nesta fazenda")
        }

        val lote = Lote(
                nome = request.nome,
                descricao = request.descricao,
                farm = farm,
                ativo = true
        )

        val salvo = loteRepository.save(lote)
        return toDto(salvo, farmId)
    }

    private fun toDto(lote: Lote, farmId: UUID): LoteDto {
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
