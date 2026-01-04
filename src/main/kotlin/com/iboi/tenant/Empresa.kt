package com.iboi.tenant

import com.iboi.auth.model.Pessoa
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "empresas")
class Empresa(

        @Id
        @GeneratedValue
        var id: UUID? = null,

        @Column(nullable = false)
        var nome: String = "",

        var trial: Boolean = true,

        var ativa: Boolean = true,

        @ManyToOne
        val proprietario: Pessoa,

        @ManyToOne
        val empresaPai: Empresa? = null,

        val criadaEm: LocalDateTime = LocalDateTime.now()
)
