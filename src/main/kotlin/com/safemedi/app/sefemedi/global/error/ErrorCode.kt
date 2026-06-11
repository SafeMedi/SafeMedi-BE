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
    INVALID_ENUM_VALUE(
        HttpStatus.BAD_REQUEST,
        "VAL_001",
        "ENUM 값이 올바르지 않습니다.",
    ),
    INVALID_DISEASE_CODE(
        HttpStatus.BAD_REQUEST,
        "VAL_002",
        "존재하지 않는 기준질환 코드가 포함되어 있습니다.",
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
    INVALID_PRESCRIPTION_DATE(
        HttpStatus.BAD_REQUEST,
        "MED_001",
        "날짜 설정이 올바르지 않습니다.",
    ),
    EMPTY_MEDICATIONS(
        HttpStatus.BAD_REQUEST,
        "MED_002",
        "분석할 약물 목록이 비어있습니다.",
    ),
    INVALID_TAKE_TIMES(
        HttpStatus.BAD_REQUEST,
        "MED_003",
        "복용 시간 목록이 비어있거나 형식이 올바르지 않습니다.",
    ),
    INVALID_PAGE_REQUEST(
        HttpStatus.BAD_REQUEST,
        "PAG_001",
        "페이지 번호(page)는 0 이상이고 크기(size)는 1 이상이어야 합니다.",
    ),
    DOCTOR_APPROVAL_REQUIRED(
        HttpStatus.BAD_REQUEST,
        "MED_009",
        "위험 요소가 발견되었으나, 의사 상담 확인(isDoctorApproved)이 누락되었습니다.",
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
