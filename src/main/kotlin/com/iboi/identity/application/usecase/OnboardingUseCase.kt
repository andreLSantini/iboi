package com.iboi.identity.application.usecase

import com.iboi.identity.api.dto.request.OnboardingRequest
import com.iboi.identity.api.dto.response.FarmSummaryDto
import com.iboi.identity.api.dto.response.FazendaDto
import com.iboi.identity.api.dto.response.OnboardingResponse
import com.iboi.identity.api.dto.response.UsuarioDto
import com.iboi.identity.api.exception.DadosInvalidosException
import com.iboi.identity.api.exception.EmailJaExisteException
import com.iboi.identity.application.service.JwtService
import com.iboi.identity.domain.*
import com.iboi.identity.infrastructure.repository.*
import com.iboi.plano.model.Assinatura
import com.iboi.plano.model.StatusAssinatura
import com.iboi.plano.model.TipoAssinatura
import com.iboi.plano.repository.AssinaturaRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Component
class OnboardingUseCase(
        private val usuarioRepository: UsuarioRepository,
        private val empresaRepository: EmpresaRepository,
        private val farmRepository: FarmRepository,
        private val userFarmProfileRepository: UserFarmProfileRepository,
        private val assinaturaRepository: AssinaturaRepository,
        private val passwordEncoder: PasswordEncoder,
        private val jwtService: JwtService,
        private val resolvePermissionsUseCase: ResolvePermissionsUseCase
) {

    @Transactional
    fun execute(request: OnboardingRequest): OnboardingResponse {

        // ✅ Validações
        validarDados(request)

        // 1️⃣ Criar empresa (tenant)
        val empresa = empresaRepository.save(
                Empresa(
                        nome = request.nomeEmpresa,
                        tipo = request.tipoEmpresa,
                        cnpj = request.cnpj
                )
        )

        // 2️⃣ Criar assinatura FREE para aquisicao self-service
        assinaturaRepository.save(
                Assinatura(
                        empresa = empresa,
                        tipo = TipoAssinatura.FREE,
                        status = StatusAssinatura.ATIVA,
                        dataInicio = LocalDateTime.now(),
                        dataVencimento = LocalDateTime.now().plusYears(50)
                )
        )

        // 3️⃣ Criar usuário ADMIN
        val usuario = usuarioRepository.save(
                Usuario(
                        nome = request.nome,
                        email = request.email,
                        telefone = request.telefone,
                        senhaHash = passwordEncoder.encode(request.senha),
                        roleEnum = RoleEnum.ADMIN,
                        empresa = empresa
                )
        )

        // 4️⃣ Criar fazenda (obrigatório)
        val farm = farmRepository.save(
                Farm(
                        name = request.nomeFazenda,
                        city = request.cidade,
                        state = request.estado,
                        productionType = request.tipoProdução,
                        size = request.tamanho,
                        empresa = empresa
                )
        )

        // 5️⃣ Vincular usuário à fazenda como ADMIN
        val profile = userFarmProfileRepository.save(
                UserFarmProfile(
                        usuario = usuario,
                        farm = farm,
                        role = FarmRole.ADMIN,
                        isDefault = true
                )
        )

        // 6️⃣ Resolver permissões
        val permissions = resolvePermissionsUseCase.execute(profile.role)

        // 7️⃣ Gerar token
        val token = jwtService.generateToken(usuario, permissions, farm.id)
        val farms = listOf(
                FarmSummaryDto(
                        id = farm.id!!,
                        name = farm.name
                )
        )

        // 8️⃣ Retornar resposta completa
        return OnboardingResponse(
                accessToken = token,
                usuario = UsuarioDto(
                        id = usuario.id!!,
                        nome = usuario.nome,
                        email = usuario.email,
                        role = usuario.roleEnum,
                        farmRole = profile.role
                ),
                fazenda = FazendaDto(
                        id = farm.id!!,
                        nome = farm.name,
                        cidade = farm.city,
                        estado = farm.state
                ),
                farms = farms,
                defaultFarmId = farm.id!!
        )
    }

    private fun validarDados(request: OnboardingRequest) {
        // Validar email único
        if (usuarioRepository.existsByEmail(request.email)) {
            throw EmailJaExisteException("O e-mail ${request.email} já está cadastrado no sistema")
        }

        // Validar campos obrigatórios
        if (request.nome.isBlank()) {
            throw DadosInvalidosException("Nome do usuário é obrigatório")
        }

        if (request.email.isBlank() || !request.email.contains("@")) {
            throw DadosInvalidosException("E-mail inválido")
        }

        if (request.senha.length < 6) {
            throw DadosInvalidosException("A senha deve ter no mínimo 6 caracteres")
        }

        if (request.nomeEmpresa.isBlank()) {
            throw DadosInvalidosException("Nome da empresa é obrigatório")
        }

        if (request.nomeFazenda.isBlank()) {
            throw DadosInvalidosException("Nome da fazenda é obrigatório")
        }

        if (request.cidade.isBlank()) {
            throw DadosInvalidosException("Cidade é obrigatória")
        }

        if (request.estado.isBlank() || request.estado.length != 2) {
            throw DadosInvalidosException("Estado deve ser uma sigla de 2 caracteres (ex: PR, SP)")
        }
    }
}
