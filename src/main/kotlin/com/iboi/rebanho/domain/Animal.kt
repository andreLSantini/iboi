package com.iboi.rebanho.domain

import com.iboi.identity.domain.Farm
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(
        name = "animais",
        uniqueConstraints = [
                UniqueConstraint(columnNames = ["brinco", "farm_id"])
        ],
        indexes = [
                Index(name = "idx_animal_brinco", columnList = "brinco"),
                Index(name = "idx_animal_status", columnList = "status"),
                Index(name = "idx_animal_farm", columnList = "farm_id")
        ]
)
class Animal(
        @Id
        @GeneratedValue
        val id: UUID? = null,

        @Column(nullable = false, length = 50)
        var brinco: String,

        @Column(length = 100)
        var nome: String? = null,

        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        val sexo: Sexo,

        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        var raca: Raca,

        @Column(nullable = false)
        var dataNascimento: LocalDate,

        @Column(precision = 10, scale = 2)
        var pesoAtual: BigDecimal? = null,

        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        var status: StatusAnimal = StatusAnimal.ATIVO,

        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        var categoria: CategoriaAnimal,

        @ManyToOne
        @JoinColumn(name = "farm_id", nullable = false)
        val farm: Farm,

        @ManyToOne
        @JoinColumn(name = "lote_id")
        var lote: Lote? = null,

        @ManyToOne
        @JoinColumn(name = "pai_id")
        var pai: Animal? = null,

        @ManyToOne
        @JoinColumn(name = "mae_id")
        var mae: Animal? = null,

        @Column(length = 1000)
        var observacoes: String? = null,

        @Column(nullable = false)
        val criadoEm: LocalDateTime = LocalDateTime.now(),

        @Column
        var atualizadoEm: LocalDateTime? = null
)
