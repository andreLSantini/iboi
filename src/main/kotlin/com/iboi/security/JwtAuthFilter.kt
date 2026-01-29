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
            val token = authHeader.substring(7)
            val claims = jwtService.extractClaims(token)

            val tenantId = UUID.fromString(claims["tenantId"].toString())
            TenantContext.set(tenantId.toString())

            val permissions = (claims["permissions"] as List<*>)
                    .map { SimpleGrantedAuthority(it.toString()) }

            val authentication = UsernamePasswordAuthenticationToken(
                    claims.subject,
                    null,
                    permissions
            )

            SecurityContextHolder.getContext().authentication = authentication
        }

        try {
            filterChain.doFilter(request, response)
        } finally {
            TenantContext.clear()
        }
    }
}
