package com.iboi.rebanho.usecase

import com.iboi.rebanho.api.exception.AcessoNegadoException
import com.iboi.rebanho.api.exception.DadosInvalidosException
import com.iboi.rebanho.api.exception.LoteNaoEncontradoException
import com.iboi.rebanho.repository.AnimalRepository
import com.iboi.rebanho.repository.LoteRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Component
class DeletarLoteUseCase(
        private val loteRepository: LoteRepository,
        private val animalRepository: AnimalRepository
) {

    @Transactional
    fun execute(loteId: UUID, farmId: UUID) {
        val lote = loteRepository.findById(loteId)
                .orElseThrow { LoteNaoEncontradoException("Lote com ID $loteId não encontrado") }

        if (lote.farm.id != farmId) {
            throw AcessoNegadoException("Você não tem permissão para deletar este lote")
        }

        // Verificar se há animais no lote
        val animaisNoLote = animalRepository.findByLoteId(loteId)
        if (animaisNoLote.isNotEmpty()) {
            throw DadosInvalidosException("Não é possível deletar um lote com animais. Remova os animais primeiro.")
        }

        loteRepository.delete(lote)
    }
}
