package com.iboi.identity.api.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.LocalDateTime

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(EmailJaExisteException::class)
    fun handleEmailJaExiste(ex: EmailJaExisteException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ErrorResponse(
                        message = ex.message ?: "E-mail já cadastrado no sistema",
                        timestamp = LocalDateTime.now()
                ))
    }

    @ExceptionHandler(DadosInvalidosException::class)
    fun handleDadosInvalidos(ex: DadosInvalidosException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse(
                        message = ex.message ?: "Dados inválidos",
                        timestamp = LocalDateTime.now()
                ))
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<ErrorResponse> {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse(
                        message = "Não foi possível concluir o cadastro. Tente novamente.",
                        timestamp = LocalDateTime.now()
                ))
    }
}

data class ErrorResponse(
        val message: String,
        val timestamp: LocalDateTime
)

class EmailJaExisteException(message: String) : RuntimeException(message)
class DadosInvalidosException(message: String) : RuntimeException(message)
