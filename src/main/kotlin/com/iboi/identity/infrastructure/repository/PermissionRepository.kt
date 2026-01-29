package com.iboi.identity.infrastructure.repository

import com.iboi.identity.domain.Permission
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface PermissionRepository : JpaRepository<Permission, UUID> {
    fun findByCode(code: String): Permission?
}