package com.iboi.perfil

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.util.*

@Entity
@Table(name = "itens_perfil")
class ItemPerfil(
        @Id
        val id: UUID = UUID.randomUUID(),

        val chave: String,
        val valor: String,

        @ManyToOne
        val perfil: PerfilUsuario
)
