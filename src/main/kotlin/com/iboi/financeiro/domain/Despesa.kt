package com.iboi.financeiro.domain

import com.iboi.identity.domain.Farm
import com.iboi.identity.domain.Usuario
import com.iboi.rebanho.domain.Animal
import com.iboi.rebanho.domain.Lote
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(
        name = "despesas",
        indexes = [
                Index(name = "idx_despesa_farm", columnList = "farm_id"),
                Index(name = "idx_despesa_data", columnList = "data"),
                Index(name = "idx_despesa_categoria", columnList = "categoria")
        ]
)
class Despesa(
        @Id
        @GeneratedValue
        val id: UUID? = null,

        @ManyToOne
        @JoinColumn(name = "farm_id", nullable = false)
        val farm: Farm,

        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        var categoria: CategoriaDespesa,

        @Column(nullable = false)
        var descricao: String,

        @Column(nullable = false, precision = 10, scale = 2)
        var valor: BigDecimal,

        @Column(nullable = false)
        var data: LocalDate,

        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        var formaPagamento: FormaPagamento,

        @ManyToOne
        @JoinColumn(name = "lote_id")
        var lote: Lote? = null,

        @ManyToOne
        @JoinColumn(name = "animal_id")
        var animal: Animal? = null,

        @ManyToOne
        @JoinColumn(name = "responsavel_id")
        val responsavel: Usuario? = null,

        @Column(length = 1000)
        var observacoes: String? = null,

        @Column(nullable = false)
        val criadoEm: LocalDateTime = LocalDateTime.now()
)
