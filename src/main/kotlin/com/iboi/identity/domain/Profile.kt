package com.iboi.identity.domain

import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "profiles")
class Profile(
        @Id @GeneratedValue
        val id: UUID? = null,

        val name: String,

        @ManyToMany(fetch = FetchType.EAGER)
        @JoinTable(
                name = "profile_permissions",
                joinColumns = [JoinColumn(name = "profile_id")],
                inverseJoinColumns = [JoinColumn(name = "permission_id")]
        )
        val permissions: Set<Permission> = emptySet()
)
