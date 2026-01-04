package com.iboi.perfil

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.*

@Entity
@Table(name = "permissoes")
class Permissao(
        @Id
        val id: UUID = UUID.randomUUID(),

        val nome: String // CRIAR_USUARIO, CRIAR_EMPRESA...
)
