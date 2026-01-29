package com.iboi.ia.domain

import com.iboi.identity.domain.Farm
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "compartilhamentos_veterinarios")
class CompartilhamentoVeterinario(
        @Id
        @GeneratedValue
        val id: UUID? = null,

        @ManyToOne
        @JoinColumn(name = "farm_id", nullable = false)
        val farm: Farm,

        @Column(nullable = false)
        val nomeVeterinario: String,

        @Column(nullable = false)
        val emailVeterinario: String,

        @Column
        var crmv: String? = null,

        @Column(nullable = false)
        val tokenAcesso: String,

        @Column(nullable = false)
        var ativo: Boolean = true,

        @Column(nullable = false)
        val criadoEm: LocalDateTime = LocalDateTime.now(),

        @Column
        var dataExpiracao: LocalDateTime? = null
)
