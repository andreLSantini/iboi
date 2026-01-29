package com.iboi.identity.api

import com.iboi.identity.api.dto.request.LoginRequest
import com.iboi.identity.application.usecase.AuthenticateUserUseCase
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping("/auth")
class AuthController(
        private val authenticateUser: AuthenticateUserUseCase
) {


    @PostMapping("/login")
    fun login(@RequestBody req: LoginRequest) =
            authenticateUser.execute(req.email, req.senha)
}
