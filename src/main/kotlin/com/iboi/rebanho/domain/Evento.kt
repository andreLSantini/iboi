package com.iboi.rebanho.domain

import com.iboi.identity.domain.Farm
import com.iboi.identity.domain.Usuario
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(
        name = "eventos",
        indexes = [
                Index(name = "idx_evento_animal", columnList = "animal_id"),
                Index(name = "idx_evento_tipo", columnList = "tipo"),
                Index(name = "idx_evento_data", columnList = "data")
        ]
)
class Evento(
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
        val tipo: TipoEvento,

        @Column(nullable = false)
        val data: LocalDate,

        @Column(length = 1000)
        var descricao: String,

        // Campos específicos para pesagem
        @Column(precision = 10, scale = 2)
        var peso: BigDecimal? = null,

        // Campos específicos para vacina/vermífugo/tratamento
        @Column(length = 200)
        var produto: String? = null,

        @Column(precision = 10, scale = 2)
        var dose: BigDecimal? = null,

        @Column(length = 50)
        var unidadeMedida: String? = null,

        // Campo para movimentação
        @ManyToOne
        @JoinColumn(name = "lote_destino_id")
        var loteDestino: Lote? = null,

        // Campo para custos
        @Column(precision = 10, scale = 2)
        var valor: BigDecimal? = null,

        @Column(length = 120)
        var reprodutorNome: String? = null,

        @Column(length = 120)
        var protocoloReprodutivo: String? = null,

        @Column
        var diagnosticoPositivo: Boolean? = null,

        @Column
        var dataPrevistaParto: LocalDate? = null,

        @Column(length = 500)
        var observacaoReprodutiva: String? = null,

        @ManyToOne
        @JoinColumn(name = "responsavel_id")
        val responsavel: Usuario? = null,

        @Column(nullable = false)
        val criadoEm: LocalDateTime = LocalDateTime.now()
)
