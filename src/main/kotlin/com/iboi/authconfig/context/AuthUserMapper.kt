package com.iboi.authconfig.context

import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component

@Component
class AuthUserMapper {

    fun from(authentication: Authentication?): AuthUser? {
        if (authentication == null || !authentication.isAuthenticated) {
            return null
        }

        val principal = authentication.principal

        if (principal !is Jwt) {
            return null
        }

        return AuthUser(
                id = principal.subject,
                email = principal.getClaim("email"),
                roles = principal.getClaimAsStringList("roles"),
                permissions = principal.getClaimAsStringList("authorities"),
                tenantId = principal.getClaim("tenant_id")
        )
    }
}
