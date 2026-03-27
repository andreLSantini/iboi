package com.iboi.identity.application.usecase

import com.iboi.identity.api.dto.request.CadastrarFazendaRequest
import com.iboi.identity.api.dto.response.FarmSummaryDto
import com.iboi.identity.domain.Farm
import com.iboi.identity.domain.FarmRole
import com.iboi.identity.domain.UserFarmProfile
import com.iboi.identity.infrastructure.repository.EmpresaRepository
import com.iboi.identity.infrastructure.repository.FarmRepository
import com.iboi.identity.infrastructure.repository.UserFarmProfileRepository
import com.iboi.identity.infrastructure.repository.UsuarioRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class CadastrarFazendaUseCase(
        private val usuarioRepository: UsuarioRepository,
        private val empresaRepository: EmpresaRepository,
        private val farmRepository: FarmRepository,
        private val userFarmProfileRepository: UserFarmProfileRepository
) {

    @Transactional
    fun execute(userId: UUID, empresaId: UUID, request: CadastrarFazendaRequest): FarmSummaryDto {
        require(request.nome.isNotBlank()) { "Nome da fazenda é obrigatório" }
        require(request.cidade.isNotBlank()) { "Cidade é obrigatória" }
        require(request.estado.length == 2) { "Estado deve ter 2 caracteres" }

        val usuario = usuarioRepository.findById(userId)
                .orElseThrow { RuntimeException("Usuário não encontrado") }
        val empresa = empresaRepository.findById(empresaId)
                .orElseThrow { RuntimeException("Empresa não encontrada") }

        val farm = farmRepository.save(
                Farm(
                        name = request.nome.trim(),
                        city = request.cidade.trim(),
                        state = request.estado.trim().uppercase(),
                        productionType = request.tipoProducao,
                        size = request.tamanho,
                        empresa = empresa
                )
        )

        userFarmProfileRepository.save(
                UserFarmProfile(
                        usuario = usuario,
                        farm = farm,
                        role = FarmRole.ADMIN,
                        isDefault = false
                )
        )

        return FarmSummaryDto(
                id = farm.id!!,
                name = farm.name
        )
    }
}
