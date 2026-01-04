package com.iboi.auth.controller

import com.iboi.auth.dto.request.LoginRequest
import com.iboi.auth.dto.response.LoginResponse
import com.iboi.auth.service.AuthService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthController(
        private val authService: AuthService
) {

    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): LoginResponse =
            authService.login(request)
}