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
    DEVICE_TOKEN_TOO_LONG(
        HttpStatus.BAD_REQUEST,
        "VAL_002",
        "deviceToken은 512자를 초과할 수 없습니다.",
    ),
    INVALID_DATE_FORMAT(
        HttpStatus.BAD_REQUEST,
        "VAL_003",
        "생년월일 형식이 올바르지 않습니다.",
    ),
    UNSUPPORTED_DEVICE_TYPE(
        HttpStatus.BAD_REQUEST,
        "VAL_003",
        "deviceType은 IOS 또는 ANDROID만 지원합니다.",
    ),
    DEVICE_TOKEN_ACCESS_DENIED(
        HttpStatus.FORBIDDEN,
        "NOTI_001",
        "해당 기기 토큰을 해제할 권한이 없습니다.",
    ),
    DEVICE_TOKEN_NOT_FOUND(
        HttpStatus.NOT_FOUND,
        "NOTI_002",
        "존재하지 않는 기기 토큰입니다.",
    ),
    NOTIFICATION_NOT_FOUND(
        HttpStatus.NOT_FOUND,
        "NOTI_003",
        "존재하지 않는 알림입니다.",
    ),
    NOTIFICATION_ACCESS_DENIED(
        HttpStatus.FORBIDDEN,
        "NOTI_004",
        "해당 알림에 접근할 권한이 없습니다.",
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
    PRESCRIPTION_NOT_FOUND(
        HttpStatus.NOT_FOUND,
        "MED_005",
        "존재하지 않거나 이미 삭제된 처방전입니다.",
    ),
    PRESCRIPTION_ACCESS_DENIED(
        HttpStatus.FORBIDDEN,
        "MED_006",
        "본인의 처방전만 수정하거나 삭제할 수 있습니다.",
    ),
    ENDED_PRESCRIPTION_UPDATE_NOT_ALLOWED(
        HttpStatus.BAD_REQUEST,
        "MED_007",
        "이미 복용 기간이 종료된 처방전의 시간은 수정할 수 없습니다.",
    ),
    PRESCRIPTION_DRUG_NOT_FOUND(
        HttpStatus.NOT_FOUND,
        "MED_010",
        "수정하려는 처방 약물이 해당 처방전에 존재하지 않습니다.",
    ),
    MEDICATION_RECORD_NOT_FOUND(
        HttpStatus.NOT_FOUND,
        "REC_001",
        "존재하지 않는 복약 기록입니다.",
    ),
    MEDICATION_RECORD_ALREADY_PROCESSED(
        HttpStatus.CONFLICT,
        "REC_002",
        "해당 시간에 이미 복용 처리가 완료된 기록입니다.",
    ),
    INVALID_TOKEN(
        HttpStatus.UNAUTHORIZED,
        "AUTH_001",
        "유효하지 않은 토큰입니다.",
    ),
    UNSUPPORTED_SOCIAL_LOGIN_PROVIDER(
        HttpStatus.BAD_REQUEST,
        "AUTH_002",
        "지원하지 않는 소셜 로그인 서비스입니다.",
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
