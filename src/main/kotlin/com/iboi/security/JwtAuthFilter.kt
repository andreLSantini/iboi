package com.iboi.security

import com.iboi.identity.application.service.JwtService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.*

@Component
class JwtAuthFilter(
        private val jwtService: JwtService
) : OncePerRequestFilter() {

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val path = request.servletPath
        return path.startsWith("/auth/")
                || path.startsWith("/onboarding")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-ui")
    }

    override fun doFilterInternal(
            request: HttpServletRequest,
            response: HttpServletResponse,
            filterChain: FilterChain
    ) {

        val authHeader = request.getHeader("Authorization")

        if (authHeader?.startsWith("Bearer ") == true) {
            try {
                val token = authHeader.substring(7)
                val claims = jwtService.extractClaims(token)

                val tenantId = UUID.fromString(claims["tenantId"].toString())
                TenantContext.set(tenantId.toString())

                val permissions = (claims["permissions"] as List<*>)
                        .map { SimpleGrantedAuthority(it.toString()) }

                val principal = AuthenticatedUser(
                        userId = UUID.fromString(claims["userId"].toString()),
                        email = claims["email"].toString(),
                        empresaId = UUID.fromString(claims["empresaId"].toString()),
                        farmId = claims["farmId"]?.toString()
                                ?.takeIf { it.isNotBlank() && it != "null" }
                                ?.let(UUID::fromString)
                )

                val authentication = UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        permissions
                )

                SecurityContextHolder.getContext().authentication = authentication
            } catch (e: Exception) {
                logger.error("Erro ao processar token JWT: ${e.message}", e)
                // Token inválido - deixa sem autenticação para retornar 401/403
            }
        }

        try {
            filterChain.doFilter(request, response)
        } finally {
            TenantContext.clear()
        }
    }
}
