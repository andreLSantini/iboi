package com.iboi.perfil

import com.iboi.auth.model.Papel
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.util.*

@Entity
@Table(name = "papeis_permissoes")
class PapelPermissao(
        @Id
        val id: UUID = UUID.randomUUID(),

        @ManyToOne
        val papel: Papel,

        @ManyToOne
        val permissao: Permissao
)
