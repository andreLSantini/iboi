package com.iboi.auth.model

import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "pessoas")
class Pessoa(
        @Id
        @GeneratedValue
        var id: UUID? = null,

        @Enumerated(EnumType.STRING)
        var tipo: TipoPessoa = TipoPessoa.CPF,

        @Column(nullable = false, unique = true)
        var documento: String = "",

        @Column(nullable = false)
        var nome: String = ""
)