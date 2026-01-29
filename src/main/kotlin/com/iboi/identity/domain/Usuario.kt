package com.iboi.identity.domain

import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "usuarios")
class Usuario(

        @Id
        @GeneratedValue
        var id: UUID? = null,

        @Column(nullable = false)
        var nome: String,

        @Column(nullable = false, unique = true)
        var email: String,

        @Column
        var telefone: String? = null,

        @Column(nullable = false)
        var senhaHash: String,

        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        var roleEnum: RoleEnum = RoleEnum.ADMIN,

        @ManyToOne(optional = false)
        var empresa: Empresa,

        var ativo: Boolean = true
)

