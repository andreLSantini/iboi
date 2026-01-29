package com.iboi.financeiro.usecase

import com.iboi.financeiro.api.dto.DespesaDto
import com.iboi.financeiro.api.dto.RegistrarDespesaRequest
import com.iboi.financeiro.domain.Despesa
import com.iboi.financeiro.repository.DespesaRepository
import com.iboi.identity.infrastructure.repository.FarmRepository
import com.iboi.identity.infrastructure.repository.UsuarioRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Component
class RegistrarDespesaUseCase(
        private val despesaRepository: DespesaRepository,
        private val farmRepository: FarmRepository,
        private val usuarioRepository: UsuarioRepository
) {

    @Transactional
    fun execute(farmId: UUID, emailUsuario: String, request: RegistrarDespesaRequest): DespesaDto {
        val farm = farmRepository.findById(farmId).orElseThrow {
            IllegalArgumentException("Fazenda não encontrada")
        }

        val responsavel = usuarioRepository.findByEmail(emailUsuario)

        val despesa = despesaRepository.save(
                Despesa(
                        farm = farm,
                        categoria = request.categoria,
                        descricao = request.descricao,
                        valor = request.valor,
                        data = request.data,
                        formaPagamento = request.formaPagamento,
                        responsavel = responsavel,
                        observacoes = request.observacoes
                )
        )

        return DespesaDto(
                id = despesa.id!!,
                categoria = despesa.categoria,
                descricao = despesa.descricao,
                valor = despesa.valor,
                data = despesa.data,
                formaPagamento = despesa.formaPagamento,
                responsavel = despesa.responsavel?.nome,
                observacoes = despesa.observacoes
        )
    }
}
