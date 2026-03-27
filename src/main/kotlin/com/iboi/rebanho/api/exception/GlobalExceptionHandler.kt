package com.iboi.rebanho.api.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.LocalDateTime

@RestControllerAdvice
class RebanhoExceptionHandler {

    @ExceptionHandler(AnimalNaoEncontradoException::class)
    fun handleAnimalNaoEncontrado(ex: AnimalNaoEncontradoException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(
                message = ex.message ?: "Animal não encontrado",
                timestamp = LocalDateTime.now()
            ))
    }

    @ExceptionHandler(BrincoDuplicadoException::class)
    fun handleBrincoDuplicado(ex: BrincoDuplicadoException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(ErrorResponse(
                message = ex.message ?: "Já existe um animal com este brinco",
                timestamp = LocalDateTime.now()
            ))
    }

    @ExceptionHandler(LoteNaoEncontradoException::class)
    fun handleLoteNaoEncontrado(ex: LoteNaoEncontradoException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(
                message = ex.message ?: "Lote não encontrado",
                timestamp = LocalDateTime.now()
            ))
    }

    @ExceptionHandler(EventoNaoEncontradoException::class)
    fun handleEventoNaoEncontrado(ex: EventoNaoEncontradoException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(
                message = ex.message ?: "Evento não encontrado",
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

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(
                message = ex.message ?: "Requisicao invalida",
                timestamp = LocalDateTime.now()
            ))
    }

    @ExceptionHandler(AcessoNegadoException::class)
    fun handleAcessoNegado(ex: AcessoNegadoException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(ErrorResponse(
                message = ex.message ?: "Acesso negado",
                timestamp = LocalDateTime.now()
            ))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationErrors(ex: MethodArgumentNotValidException): ResponseEntity<ValidationErrorResponse> {
        val errors = ex.bindingResult.allErrors.associate { error ->
            val fieldName = (error as FieldError).field
            val errorMessage = error.defaultMessage ?: "Campo inválido"
            fieldName to errorMessage
        }

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ValidationErrorResponse(
                message = "Erro de validação",
                errors = errors,
                timestamp = LocalDateTime.now()
            ))
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<ErrorResponse> {
        // Log da exceção para debugging
        ex.printStackTrace()

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse(
                message = "Erro interno do servidor. Tente novamente.",
                timestamp = LocalDateTime.now()
            ))
    }
}

data class ErrorResponse(
    val message: String,
    val timestamp: LocalDateTime
)

data class ValidationErrorResponse(
    val message: String,
    val errors: Map<String, String>,
    val timestamp: LocalDateTime
)

// Exceções customizadas
class AnimalNaoEncontradoException(message: String) : RuntimeException(message)
class BrincoDuplicadoException(message: String) : RuntimeException(message)
class LoteNaoEncontradoException(message: String) : RuntimeException(message)
class EventoNaoEncontradoException(message: String) : RuntimeException(message)
class DadosInvalidosException(message: String) : RuntimeException(message)
class AcessoNegadoException(message: String) : RuntimeException(message)
