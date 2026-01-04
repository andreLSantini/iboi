package com.iboi.auth.model

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.util.*

@Entity
@Table(name = "usuarios_papeis")
class UsuarioPapel(
        @Id
        val id: UUID = UUID.randomUUID(),

        @ManyToOne
        val usuario: Usuario,

        @ManyToOne
        val papel: Papel
)
