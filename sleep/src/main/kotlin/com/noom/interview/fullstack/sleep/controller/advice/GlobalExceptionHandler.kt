package com.noom.interview.fullstack.sleep.controller.advice

import com.noom.interview.fullstack.sleep.service.exception.DuplicateResourceException
import com.noom.interview.fullstack.sleep.service.exception.InvalidInputException
import com.noom.interview.fullstack.sleep.service.exception.ResourceNotFoundException
import java.time.LocalDateTime
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest

data class ErrorResponse(
        val timestamp: LocalDateTime = LocalDateTime.now(),
        val status: Int,
        val error: String,
        val message: String?,
        val path: String,
        val details: List<String>? = null
)

@ControllerAdvice
class GlobalExceptionHandler {

    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleResourceNotFoundException(
            ex: ResourceNotFoundException,
            request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Resource not found: ${ex.message}")
        val errorDetails =
                ErrorResponse(
                        status = HttpStatus.NOT_FOUND.value(),
                        error = HttpStatus.NOT_FOUND.reasonPhrase,
                        message = ex.message,
                        path = request.getDescription(false).substringAfter("uri=")
                )
        return ResponseEntity(errorDetails, HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(DuplicateResourceException::class)
    fun handleDuplicateResourceException(
            ex: DuplicateResourceException,
            request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Duplicate resource attempt: ${ex.message}")
        val errorDetails =
                ErrorResponse(
                        status = HttpStatus.CONFLICT.value(),
                        error = HttpStatus.CONFLICT.reasonPhrase,
                        message = ex.message,
                        path = request.getDescription(false).substringAfter("uri=")
                )
        return ResponseEntity(errorDetails, HttpStatus.CONFLICT)
    }

    @ExceptionHandler(InvalidInputException::class)
    fun handleInvalidInputException(
            ex: InvalidInputException,
            request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Invalid input: ${ex.message}")
        val errorDetails =
                ErrorResponse(
                        status = HttpStatus.BAD_REQUEST.value(),
                        error = HttpStatus.BAD_REQUEST.reasonPhrase,
                        message = ex.message,
                        path = request.getDescription(false).substringAfter("uri=")
                )
        return ResponseEntity(errorDetails, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValid(
            ex: MethodArgumentNotValidException,
            request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Validation error: ${ex.message}")
        val errorMessages = ex.bindingResult.fieldErrors.map { "${it.field}: ${it.defaultMessage}" }
        val errorDetails =
                ErrorResponse(
                        status = HttpStatus.BAD_REQUEST.value(),
                        error = "Validation Error",
                        message = "Input validation failed. See details.",
                        path = request.getDescription(false).substringAfter("uri="),
                        details = errorMessages
                )
        return ResponseEntity(errorDetails, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception, request: WebRequest): ResponseEntity<ErrorResponse> {
        logger.error("An unexpected error occurred: ${ex.message}", ex)
        val errorDetails =
                ErrorResponse(
                        status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        error = HttpStatus.INTERNAL_SERVER_ERROR.reasonPhrase,
                        message =
                                "An unexpected internal server error occurred. Please try again later.",
                        path = request.getDescription(false).substringAfter("uri=")
                )
        return ResponseEntity(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR)
    }
}
