package com.iboi.plano.repository

import com.iboi.plano.model.Pagamento
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface PagamentoRepository : JpaRepository<Pagamento, UUID> {
    fun findByAssinaturaIdOrderByDataVencimentoDesc(assinaturaId: UUID): List<Pagamento>
    fun findByAssinaturaEmpresaIdOrderByDataVencimentoDesc(empresaId: UUID): List<Pagamento>
}
