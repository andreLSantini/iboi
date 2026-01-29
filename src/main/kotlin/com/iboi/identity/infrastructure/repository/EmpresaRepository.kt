package com.iboi.identity.infrastructure.repository

import com.iboi.identity.domain.Empresa
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface EmpresaRepository : JpaRepository<Empresa, UUID> {


}