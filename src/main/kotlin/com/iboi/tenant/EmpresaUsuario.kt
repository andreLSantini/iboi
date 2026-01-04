package com.iboi.tenant

import com.iboi.auth.model.Usuario
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.util.*

@Entity
@Table(name = "empresas_usuarios")
class EmpresaUsuario(
        @Id
        val id: UUID = UUID.randomUUID(),

        @ManyToOne
        val empresa: Empresa,

        @ManyToOne
        val usuario: Usuario
)
