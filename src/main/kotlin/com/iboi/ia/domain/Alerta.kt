package com.iboi.ia.domain

import com.iboi.identity.domain.Farm
import com.iboi.rebanho.domain.Animal
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(
        name = "alertas",
        indexes = [
                Index(name = "idx_alerta_farm", columnList = "farm_id"),
                Index(name = "idx_alerta_status", columnList = "status"),
                Index(name = "idx_alerta_prioridade", columnList = "prioridade")
        ]
)
class Alerta(
        @Id
        @GeneratedValue
        val id: UUID? = null,

        @ManyToOne
        @JoinColumn(name = "farm_id", nullable = false)
        val farm: Farm,

        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        val tipo: TipoAlerta,

        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        val prioridade: PrioridadeAlerta,

        @Column(nullable = false)
        val titulo: String,

        @Column(length = 1000)
        val mensagem: String,

        @ManyToOne
        @JoinColumn(name = "animal_id")
        val animal: Animal? = null,

        @Column(length = 500)
        var recomendacao: String? = null,

        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        var status: StatusAlerta = StatusAlerta.ATIVO,

        @Column(nullable = false)
        val criadoEm: LocalDateTime = LocalDateTime.now(),

        @Column
        var lidoEm: LocalDateTime? = null,

        @Column
        var resolvidoEm: LocalDateTime? = null
)
