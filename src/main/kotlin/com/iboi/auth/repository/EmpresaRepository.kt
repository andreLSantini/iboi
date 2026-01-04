package com.iboi.auth.repository

import com.iboi.tenant.Empresa
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface EmpresaRepository : JpaRepository<Empresa, UUID>
