package com.iboi.auth.model

import com.iboi.tenant.Empresa
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "usuarios")
class Usuario(
        @Id
        @GeneratedValue
        var id: UUID? = null,

        @Column(nullable = false, unique = true)
        var email: String = "",

        @Column(nullable = false)
        var senhaHash: String = "",

        var ativo: Boolean = true,

        @ManyToOne
        var pessoa: Pessoa? = null,

        @ManyToOne
        var empresa: Empresa? = null
)
