package com.iboi.auth.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.*

@Entity
@Table(name = "papeis")
class Papel(
        @Id
        val id: UUID = UUID.randomUUID(),

        @Column(unique = true)
        val nome: String // ADMIN, VETERINARIO, OPERADOR...
)
