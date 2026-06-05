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
        "요청 값이 올바르지 않습니다.",
    ),
    INVALID_ENUM_VALUE(
        HttpStatus.BAD_REQUEST,
        "VAL_001",
        "ENUM 값이 올바르지 않습니다.",
    ),
    INVALID_DISEASE_CODE(
        HttpStatus.BAD_REQUEST,
        "VAL_002",
        "존재하지 않는 기저질환 코드가 포함되어 있습니다.",
    ),
    INVALID_DATE_FORMAT(
        HttpStatus.BAD_REQUEST,
        "VAL_003",
        "생년월일 형식이 올바르지 않습니다.",
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
        "유효하지 않은 토큰입니다.",
    ),
    INVALID_ACCESS_TOKEN(
        HttpStatus.UNAUTHORIZED,
        "AUTH_003",
        "유효하지 않은 액세스 토큰입니다. 다시 로그인해 주세요.",
    ),
    USER_NOT_FOUND(
        HttpStatus.NOT_FOUND,
        "USER_001",
        "존재하지 않는 사용자 정보입니다.",
    ),
    INTERNAL_SERVER_ERROR(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "SYS_500",
        "서버 내부 오류가 발생했습니다.",
    ),
}
