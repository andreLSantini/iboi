package com.iboi.identity.domain

import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "roles")
class Role(

        @Id
        @GeneratedValue
        val id: UUID? = null,

        @Column(nullable = false, unique = true)
        val name: String
)