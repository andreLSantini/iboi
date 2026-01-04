package com.iboi.perfil

import com.iboi.auth.model.Usuario
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.util.*

@Entity
@Table(name = "perfil_usuario")
class PerfilUsuario(
        @Id
        val id: UUID = UUID.randomUUID(),

        @OneToOne
        val usuario: Usuario,

        val telefone: String?,
        val avatarUrl: String?
)
