package com.iboi.identity.infrastructure.repository

import com.iboi.identity.domain.Profile
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface ProfileRepository : JpaRepository<Profile, UUID>