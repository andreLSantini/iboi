package com.iboi.identity.domain

import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "role_permissions")
class RolePermission(

        @Id
        @GeneratedValue
        val id: UUID? = null,

        @ManyToOne
        val role: Role,

        @ManyToOne
        val permission: Permission
)