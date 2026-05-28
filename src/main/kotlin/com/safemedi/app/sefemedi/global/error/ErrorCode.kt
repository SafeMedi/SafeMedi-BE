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
        "?낅젰 媛믪씠 ?щ컮瑜댁? ?딆뒿?덈떎.",
    ),
    INVALID_ENUM_VALUE(
        HttpStatus.BAD_REQUEST,
        "VAL_001",
        "ENUM 媛믪씠 ?щ컮瑜댁? ?딆뒿?덈떎.",
    ),
    INVALID_DISEASE_CODE(
        HttpStatus.BAD_REQUEST,
        "VAL_002",
        "議댁옱?섏? ?딅뒗 湲곗?吏덊솚 肄붾뱶媛 ?ы븿?섏뼱 ?덉뒿?덈떎.",
    ),
    INVALID_DATE_FORMAT(
        HttpStatus.BAD_REQUEST,
        "VAL_003",
        "생년월일 형식이 올바르지 않습니다.",
    ),
    TUTORIAL_ALREADY_COMPLETED(
        HttpStatus.BAD_REQUEST,
        "TUT_001",
        "?대? ?꾨즺???쒗넗由ъ뼹?낅땲??",
    ),
    INVALID_SEARCH_KEYWORD(
        HttpStatus.BAD_REQUEST,
        "VAL_005",
        "寃?됱뼱??理쒖냼 2湲???댁긽 ?낅젰?댁빞 ?⑸땲??",
    ),
    INVALID_TOKEN(
        HttpStatus.UNAUTHORIZED,
        "AUTH_001",
        "?좏슚?섏? ?딄굅??留뚮즺???좏겙?낅땲??",
    ),
    INTERNAL_SERVER_ERROR(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "SYS_500",
        "?쒕쾭 ?대? ?ㅻ쪟媛 諛쒖깮?덉뒿?덈떎.",
    ),
}
