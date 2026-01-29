package com.iboi.identity.infrastructure.repository

import com.iboi.identity.domain.RolePermission
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface RolePermissionRepository : JpaRepository<RolePermission, UUID> {

    fun findByRoleId(roleId: UUID?): RolePermission

    fun findByRole_Id(roleId: UUID): List<RolePermission>
}