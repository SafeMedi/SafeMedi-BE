package com.safemedi.app.sefemedi.global.error

import org.springframework.http.HttpStatus

enum class ErrorCode(
    val status: HttpStatus,
    val code: String,
    val message: String,
) {
    INVALID_REQUEST(
        HttpStatus.BAD_REQUEST,
        "VAL_001",
        "입력 값이 올바르지 않습니다.",
    ),
    TUTORIAL_ALREADY_COMPLETED(
        HttpStatus.BAD_REQUEST,
        "TUT_001",
        "이미 완료된 튜토리얼입니다.",
    ),
    INVALID_SEARCH_KEYWORD(
        HttpStatus.BAD_REQUEST,
        "VAL_005",
        "검색어는 최소 2글자 이상 입력해야 합니다.",
    ),
    INVALID_TOKEN(
        HttpStatus.UNAUTHORIZED,
        "AUTH_001",
        "유효하지 않거나 만료된 토큰입니다.",
    ),
    INTERNAL_SERVER_ERROR(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "SYS_500",
        "서버 내부 오류가 발생했습니다.",
    ),
}
