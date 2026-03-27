package com.iboi.rebanho.domain

import com.iboi.identity.domain.Farm
import com.iboi.identity.domain.Pasture
import com.iboi.identity.domain.Usuario
import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(
        name = "movimentacoes_animais",
        indexes = [
                Index(name = "idx_movimentacao_animal", columnList = "animal_id"),
                Index(name = "idx_movimentacao_farm", columnList = "farm_origem_id,farm_destino_id"),
                Index(name = "idx_movimentacao_data", columnList = "movimentada_em")
        ]
)
class MovimentacaoAnimal(
        @Id
        @GeneratedValue
        val id: UUID? = null,

        @ManyToOne
        @JoinColumn(name = "animal_id", nullable = false)
        val animal: Animal,

        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        val tipo: TipoMovimentacaoAnimal,

        @ManyToOne
        @JoinColumn(name = "farm_origem_id")
        val farmOrigem: Farm? = null,

        @ManyToOne
        @JoinColumn(name = "farm_destino_id")
        val farmDestino: Farm? = null,

        @ManyToOne
        @JoinColumn(name = "pasture_origem_id")
        val pastureOrigem: Pasture? = null,

        @ManyToOne
        @JoinColumn(name = "pasture_destino_id")
        val pastureDestino: Pasture? = null,

        @Column(name = "movimentada_em", nullable = false)
        val movimentadaEm: LocalDate,

        @Column(name = "numero_gta", length = 64)
        val numeroGta: String? = null,

        @Column(name = "documento_externo", length = 128)
        val documentoExterno: String? = null,

        @Column(length = 255)
        val motivo: String? = null,

        @Column(length = 1000)
        val observacoes: String? = null,

        @ManyToOne
        @JoinColumn(name = "responsavel_id")
        val responsavel: Usuario? = null,

        @Column(nullable = false)
        val criadoEm: LocalDateTime = LocalDateTime.now()
)
