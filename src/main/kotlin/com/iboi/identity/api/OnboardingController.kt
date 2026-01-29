package com.iboi.identity.api

import com.iboi.identity.api.dto.request.OnboardingRequest
import com.iboi.identity.application.usecase.OnboardingUseCase
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/onboarding")
@Tag(name = "Onboarding", description = "Cadastro inicial com trial de 30 dias")
class OnboardingController(
        private val onboardingUseCase: OnboardingUseCase
) {

    @PostMapping
    @Operation(summary = "Cadastrar nova empresa", description = "Cria empresa, fazenda, usuário admin e assinatura trial de 30 dias")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Cadastro realizado com sucesso"),
        ApiResponse(responseCode = "409", description = "Email já cadastrado"),
        ApiResponse(responseCode = "400", description = "Dados inválidos")
    ])
    fun onboard(@RequestBody request: OnboardingRequest) =
            onboardingUseCase.execute(request)
}