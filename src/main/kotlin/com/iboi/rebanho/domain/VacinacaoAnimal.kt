package com.iboi.rebanho.domain

import com.iboi.identity.domain.Farm
import com.iboi.identity.domain.Usuario
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(
        name = "vacinacoes_animais",
        indexes = [
                Index(name = "idx_vacinacao_animal", columnList = "animal_id"),
                Index(name = "idx_vacinacao_farm", columnList = "farm_id"),
                Index(name = "idx_vacinacao_data", columnList = "aplicada_em"),
                Index(name = "idx_vacinacao_tipo", columnList = "tipo")
        ]
)
class VacinacaoAnimal(
        @Id
        @GeneratedValue
        val id: UUID? = null,

        @ManyToOne
        @JoinColumn(name = "animal_id", nullable = false)
        val animal: Animal,

        @ManyToOne
        @JoinColumn(name = "farm_id", nullable = false)
        val farm: Farm,

        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        val tipo: TipoVacina,

        @Column(name = "nome_vacina", nullable = false, length = 120)
        val nomeVacina: String,

        @Column(precision = 10, scale = 2)
        val dose: BigDecimal? = null,

        @Column(name = "unidade_medida", length = 30)
        val unidadeMedida: String? = null,

        @Column(name = "aplicada_em", nullable = false)
        val aplicadaEm: LocalDate,

        @Column(name = "proxima_dose_em")
        val proximaDoseEm: LocalDate? = null,

        @Column(length = 120)
        val fabricante: String? = null,

        @Column(name = "lote_vacina", length = 80)
        val loteVacina: String? = null,

        @Column(length = 1000)
        val observacoes: String? = null,

        @ManyToOne
        @JoinColumn(name = "responsavel_id")
        val responsavel: Usuario? = null,

        @Column(nullable = false)
        val criadoEm: LocalDateTime = LocalDateTime.now()
)
