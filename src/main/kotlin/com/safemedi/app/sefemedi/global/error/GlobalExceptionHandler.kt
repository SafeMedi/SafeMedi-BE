package com.safemedi.app.sefemedi.global.error

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(exception: BusinessException): ResponseEntity<ErrorResponse> {
        val errorCode = exception.errorCode
        return ResponseEntity.status(errorCode.status)
            .body(ErrorResponse(errorCode.code, errorCode.message))
    }

    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleMissingRequestParameter(): ResponseEntity<ErrorResponse> {
        val errorCode = ErrorCode.INVALID_SEARCH_KEYWORD
        return ResponseEntity.status(errorCode.status)
            .body(ErrorResponse(errorCode.code, errorCode.message))
    }

    @ExceptionHandler(Exception::class)
    fun handleException(): ResponseEntity<ErrorResponse> {
        val errorCode = ErrorCode.INTERNAL_SERVER_ERROR
        return ResponseEntity.status(errorCode.status)
            .body(ErrorResponse(errorCode.code, errorCode.message))
    }
}
