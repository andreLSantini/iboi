package com.iboi.identity.infrastructure.repository

import com.iboi.identity.domain.Usuario
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface UsuarioRepository : JpaRepository<Usuario, UUID> {
    fun findByEmail(email: String): Usuario?
    fun existsByEmail(email: String): Boolean
}