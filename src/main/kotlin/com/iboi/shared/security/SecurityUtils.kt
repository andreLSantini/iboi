package com.iboi.shared.security

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt

object SecurityUtils {

    fun currentUserId(): String =
            (SecurityContextHolder.getContext().authentication?.principal as Jwt).subject
}
