package com.iboi.identity.domain

import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "permissions")
class Permission(
        @Id @GeneratedValue
        val id: UUID? = null,

        @Column(unique = true)
        val code: String, // ex: VIEW_CATTLE

        val description: String
)
