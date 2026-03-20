package com.iboi.identity.application.service

import com.iboi.identity.domain.Usuario
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*
import java.util.UUID

@Service
class JwtService(

        @Value("\${spring.security.jwt.secret}")
        private val secret: String,

        @Value("\${spring.security.jwt.expiration}")
        private val expiration: Long
) {

    fun generateToken(user: Usuario, permissions: List<String>, defaultFarmId: UUID? = null): String {
        return Jwts.builder()
                .setSubject(user.id.toString())
                .claim("email", user.email)
                .claim("empresaId", user.empresa.id.toString())
                .claim("role", user.roleEnum.name)
                .claim("userId", user.id.toString())
                .claim("tenantId", user.empresa.id.toString())
                .claim("farmId", defaultFarmId?.toString())
                .claim("permissions", permissions)
                .setIssuedAt(Date())
                .setExpiration(Date(System.currentTimeMillis() + expiration))
                .signWith(Keys.hmacShaKeyFor(secret.toByteArray()))
                .compact()
    }
    fun extractUserId(token: String): UUID =
            UUID.fromString(
                    Jwts.parserBuilder()
                            .setSigningKey(secret.toByteArray())
                            .build() .parseClaimsJws(token) .body .subject )

    fun extractClaims(token: String) =
            Jwts.parserBuilder()
                    .setSigningKey(secret.toByteArray())
                    .build()
                    .parseClaimsJws(token)
                    .body

}
