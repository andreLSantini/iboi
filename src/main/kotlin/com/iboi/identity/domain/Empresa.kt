package com.iboi.identity.domain

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "empresas")
class Empresa(

        @Id
        @GeneratedValue
        var id: UUID? = null,

        @Column(nullable = false)
        var nome: String,

        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        var tipo: EmpresaType = EmpresaType.MATRIZ,

        @Column(unique = true)
        var cnpj: String? = null,

        @Column(name = "empresa_matriz_id")
        var empresaMatrizId: UUID? = null,

        @Column(name = "asaas_customer_id")
        var asaasCustomerId: String? = null,

        var ativa: Boolean = true,

        val criadaEm: LocalDateTime = LocalDateTime.now()
)
