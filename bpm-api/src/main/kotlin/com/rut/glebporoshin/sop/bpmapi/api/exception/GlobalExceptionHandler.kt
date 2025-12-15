package com.rut.glebporoshin.sop.bpmapi.api.exception

import com.rut.glebporoshin.sop.bpmapi.api.dto.common.StatusResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(ApiException::class)
    fun handleApiException(ex: ApiException): ResponseEntity<StatusResponse> {
        val status = HttpStatus.valueOf(ex.errorCode.httpStatus)
        return ResponseEntity
            .status(status)
            .body(StatusResponse(status = "error", error = ex.message))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<StatusResponse> {
        val status = HttpStatus.BAD_REQUEST
        val message = ex.bindingResult.fieldErrors.joinToString("; ") { error ->
            "${error.field}: ${error.defaultMessage ?: "Invalid value"}"
        }.ifBlank { "Validation failed" }
        return ResponseEntity
            .status(status)
            .body(
                StatusResponse(
                    status = "error",
                    error = message,
                ),
            )
    }

    @ExceptionHandler(Exception::class)
    fun handleUnexpected(ex: Exception): ResponseEntity<StatusResponse> {
        val status = HttpStatus.INTERNAL_SERVER_ERROR
        val message = ex.message ?: status.reasonPhrase
        return ResponseEntity
            .status(status)
            .body(StatusResponse(status = "error", error = message))
    }
}
