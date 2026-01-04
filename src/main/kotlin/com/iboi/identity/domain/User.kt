package com.iboi.identity.domain

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.*

@Entity
@Table(name = "users")
class User(
        @Id @GeneratedValue
        val id: UUID? = null,

        val email: String,
        val password: String,
        val active: Boolean = true
)
