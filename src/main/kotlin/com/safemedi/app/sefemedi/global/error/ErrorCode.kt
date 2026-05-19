package com.safemedi.app.sefemedi.global.error

import org.springframework.http.HttpStatus

enum class ErrorCode(
    val status: HttpStatus,
    val code: String,
    val message: String,
) {
    INVALID_SEARCH_KEYWORD(
        HttpStatus.BAD_REQUEST,
        "VAL_005",
        "검색어는 최소 2글자 이상 입력해야 합니다.",
    ),
    INTERNAL_SERVER_ERROR(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "SYS_500",
        "서버 내부 오류가 발생했습니다.",
    ),
}
