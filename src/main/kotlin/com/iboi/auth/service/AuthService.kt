package com.iboi.auth.service

import com.iboi.auth.dto.request.LoginRequest
import com.iboi.auth.dto.response.LoginResponse
import com.iboi.auth.repository.UsuarioRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
        private val usuarioRepository: UsuarioRepository,
        private val passwordEncoder: PasswordEncoder,
        private val jwtService: JwtService
) {

    fun login(request: LoginRequest): LoginResponse {
        val user = usuarioRepository.findByEmail(request.email)
                ?: throw RuntimeException("Usuário não encontrado")

        if (!passwordEncoder.matches(request.senha, user.senhaHash)) {
            throw RuntimeException("Senha inválida")
        }

        val token = jwtService.generateToken(user)
        return LoginResponse(token)
    }
}
