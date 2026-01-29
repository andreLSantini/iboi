package com.iboi.security

import com.fasterxml.jackson.databind.ObjectMapper
import com.iboi.identity.infrastructure.repository.UsuarioRepository
import com.iboi.plano.service.AssinaturaService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.time.LocalDateTime

@Component
class AssinaturaFilter(
        private val assinaturaService: AssinaturaService,
        private val usuarioRepository: UsuarioRepository,
        private val objectMapper: ObjectMapper
) : OncePerRequestFilter() {

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val path = request.servletPath
        return path.startsWith("/auth/")
                || path.startsWith("/onboarding")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/h2-console")
    }

    override fun doFilterInternal(
            request: HttpServletRequest,
            response: HttpServletResponse,
            filterChain: FilterChain
    ) {
        val authentication = SecurityContextHolder.getContext().authentication

        if (authentication?.isAuthenticated == true) {
            val email = authentication.principal as? String

            if (email != null) {
                val usuario = usuarioRepository.findByEmail(email)
                val empresaId = usuario?.empresa?.id

                if (empresaId != null) {
                    val assinaturaAtiva = assinaturaService.isAssinaturaAtiva(empresaId)

                    if (!assinaturaAtiva) {
                        response.status = HttpServletResponse.SC_PAYMENT_REQUIRED
                        response.contentType = "application/json"
                        response.characterEncoding = "UTF-8"

                        val errorResponse = mapOf(
                                "message" to "Assinatura vencida. Realize o pagamento para continuar usando o sistema.",
                                "timestamp" to LocalDateTime.now().toString()
                        )

                        response.writer.write(objectMapper.writeValueAsString(errorResponse))
                        return
                    }
                }
            }
        }

        filterChain.doFilter(request, response)
    }
}
