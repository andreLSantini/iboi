package com.iboi.sanitario.domain

import com.iboi.rebanho.domain.CategoriaAnimal
import jakarta.persistence.*
import java.math.BigDecimal
import java.util.*

@Entity
@Table(name = "itens_protocolo")
class ItemProtocolo(
        @Id
        @GeneratedValue
        val id: UUID? = null,

        @ManyToOne
        @JoinColumn(name = "protocolo_id", nullable = false)
        val protocolo: ProtocoloSanitario,

        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        val tipo: TipoAplicacao,

        @Column(nullable = false, length = 200)
        var produto: String,

        @Column(precision = 10, scale = 2)
        var dose: BigDecimal? = null,

        @Column(length = 50)
        var unidadeMedida: String? = null,

        @Column(nullable = false)
        var idadeAplicacaoDias: Int, // Idade em dias para aplicação

        @Column(nullable = false)
        var periodicidadeDias: Int? = null, // Se é recorrente (ex: a cada 90 dias)

        @ElementCollection(fetch = FetchType.EAGER)
        @CollectionTable(name = "item_protocolo_categorias", joinColumns = [JoinColumn(name = "item_id")])
        @Enumerated(EnumType.STRING)
        @Column(name = "categoria")
        var categoriasAplicaveis: MutableSet<CategoriaAnimal> = mutableSetOf(),

        @Column(length = 500)
        var observacoes: String? = null
)
