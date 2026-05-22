package com.safemedi.app.sefemedi.global.exception

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException::class)
    fun handle(
        e: RuntimeException
    ): ResponseEntity<Any> {

        return when (e.message) {

            "VAL_001" ->
                ResponseEntity.badRequest().body(
                    mapOf(
                        "code" to "VAL_001",
                        "message" to "입력 값이 올바르지 않습니다."
                    )
                )

            "TUT_001" ->
                ResponseEntity.badRequest().body(
                    mapOf(
                        "code" to "TUT_001",
                        "message" to "이미 완료된 튜토리얼입니다."
                    )
                )

            "AUTH_001" ->
                ResponseEntity.status(401).body(
                    mapOf(
                        "code" to "AUTH_001",
                        "message" to "유효하지 않거나 만료된 토큰입니다."
                    )
                )

            else ->
                ResponseEntity.internalServerError().body(
                    mapOf(
                        "message" to "서버 오류"
                    )
                )
        }
    }
}
