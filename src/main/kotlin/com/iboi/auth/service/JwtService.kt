package com.iboi.auth.service

import com.iboi.auth.model.Usuario
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*

@Service
class JwtService {

    @Value("\${spring.security.jwt.secret}")
    private val secret: String = ""

    @Value("\${spring.security.jwt.expiration}")
    private val expiration: Long = 0

    fun generateToken(user: Usuario): String {
        return Jwts.builder()
                .setSubject(user.id.toString())
                .claim("email", user.email)
                .setIssuedAt(Date())
                .setExpiration(Date(System.currentTimeMillis() + expiration))
                .signWith(Keys.hmacShaKeyFor(secret.toByteArray()))
                .compact()
    }

    fun extractUserId(token: String): UUID =
            UUID.fromString(
                    Jwts.parserBuilder()
                            .setSigningKey(secret.toByteArray())
                            .build()
                            .parseClaimsJws(token)
                            .body
                            .subject
            )
}
