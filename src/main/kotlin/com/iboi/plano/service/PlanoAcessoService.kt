package com.iboi.plano.service

import com.iboi.plano.model.PlanoCatalogo
import com.iboi.plano.model.PlanoRecurso
import com.iboi.plano.model.StatusAssinatura
import com.iboi.rebanho.repository.AnimalRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

data class PlanoAcessoResumo(
        val titulo: String,
        val descricao: String,
        val limiteAnimais: Int?,
        val animaisCadastrados: Long,
        val recursos: Set<PlanoRecurso>
)

@Service
class PlanoAcessoService(
        private val assinaturaService: AssinaturaService,
        private val animalRepository: AnimalRepository
) {

    fun resumo(empresaId: UUID): PlanoAcessoResumo {
        val assinatura = assinaturaService.getAssinatura(empresaId)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Assinatura nao encontrada")
        val definicao = PlanoCatalogo.get(assinatura.tipo)

        return PlanoAcessoResumo(
                titulo = definicao.titulo,
                descricao = definicao.descricao,
                limiteAnimais = definicao.limiteAnimais,
                animaisCadastrados = animalRepository.countByEmpresaId(empresaId),
                recursos = definicao.recursos
        )
    }

    fun requireRecurso(empresaId: UUID, recurso: PlanoRecurso, mensagem: String? = null) {
        val assinatura = assinaturaService.getAssinatura(empresaId)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Assinatura nao encontrada")

        if (assinatura.status == StatusAssinatura.CANCELADA || assinatura.status == StatusAssinatura.SUSPENSA) {
            throw ResponseStatusException(HttpStatus.PAYMENT_REQUIRED, "Assinatura inativa para este recurso")
        }

        val definicao = PlanoCatalogo.get(assinatura.tipo)
        if (!definicao.recursos.contains(recurso)) {
            throw ResponseStatusException(
                    HttpStatus.PAYMENT_REQUIRED,
                    mensagem ?: "Seu plano atual nao inclui este recurso"
            )
        }
    }

    fun requireCapacidadeAnimais(empresaId: UUID, quantidadeNovosAnimais: Int = 1) {
        val resumo = resumo(empresaId)
        val limite = resumo.limiteAnimais ?: return
        if (resumo.animaisCadastrados + quantidadeNovosAnimais > limite) {
            throw ResponseStatusException(
                    HttpStatus.PAYMENT_REQUIRED,
                    "O plano ${resumo.titulo} permite ate $limite animais. Faca upgrade para continuar."
            )
        }
    }
}
