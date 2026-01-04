package com.iboi.auth.controller

import com.iboi.auth.dto.request.SignupRequest
import com.iboi.auth.dto.response.SignupResponse
import com.iboi.auth.service.SignupService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class SignupController(
        private val signupService: SignupService
) {

    @PostMapping("/signup")
    fun signup(@RequestBody request: SignupRequest): SignupResponse =
            signupService.signup(request)
}
