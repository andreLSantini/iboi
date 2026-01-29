package com.iboi.ia.domain

import com.iboi.rebanho.domain.Animal
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "scores_risco_sanitario")
class ScoreRiscoSanitario(
        @Id
        @GeneratedValue
        val id: UUID? = null,

        @OneToOne
        @JoinColumn(name = "animal_id", nullable = false, unique = true)
        val animal: Animal,

        @Column(nullable = false)
        var score: Int, // 0-100 (0 = sem risco, 100 = risco crítico)

        @Column(length = 500)
        var fatoresRisco: String? = null,

        @Column(nullable = false)
        val calculadoEm: LocalDateTime = LocalDateTime.now()
)
