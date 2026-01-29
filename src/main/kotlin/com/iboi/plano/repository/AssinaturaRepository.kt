package com.iboi.plano.repository

import com.iboi.plano.model.Assinatura
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface AssinaturaRepository : JpaRepository<Assinatura, UUID> {
    fun findByEmpresaId(empresaId: UUID): Assinatura?
}
