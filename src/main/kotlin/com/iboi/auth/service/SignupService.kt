package com.iboi.auth.service

import com.iboi.auth.dto.request.SignupRequest
import com.iboi.auth.dto.response.SignupResponse
import com.iboi.auth.model.Pessoa
import com.iboi.auth.model.Usuario
import com.iboi.auth.repository.EmpresaRepository
import com.iboi.auth.repository.PessoaRepository
import com.iboi.auth.repository.UsuarioRepository
import com.iboi.tenant.Empresa
import jakarta.transaction.Transactional
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class SignupService(
        private val pessoaRepository: PessoaRepository,
        private val empresaRepository: EmpresaRepository,
        private val usuarioRepository: UsuarioRepository,
        private val passwordEncoder: PasswordEncoder,
        private val jwtService: JwtService
) {

    @Transactional
    fun signup(request: SignupRequest): SignupResponse {

        if (usuarioRepository.findByEmail(request.email) != null) {
            throw RuntimeException("Email já cadastrado")
        }

        val pessoa = pessoaRepository.findByDocumento(request.documento)
                ?: pessoaRepository.save(
                        Pessoa(
                                nome = request.nomePessoa,
                                tipo = request.tipoPessoa,
                                documento = request.documento
                        )
                )
        val empresaPai = request.empresaPaiId?.let {
            empresaRepository.findById(it)
                    .orElseThrow { RuntimeException("Empresa pai não encontrada") }
        }

        val empresa = empresaRepository.save(
                Empresa(
                        nome = request.nomeEmpresa,
                        trial = true,
                        ativa = true,
                        proprietario = pessoa,
                        empresaPai = empresaPai
                )
        )

        val usuario = usuarioRepository.save(
                Usuario(
                        email = request.email,
                        senhaHash = passwordEncoder.encode(request.senha),
                        pessoa = pessoa,
                        empresa = empresa
                )
        )

        val token = jwtService.generateToken(usuario)
        return SignupResponse(token)
    }
}
