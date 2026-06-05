package com.safemedi.app.sefemedi.domain.drug.dto

data class DrugSearchResponse(
    val drugCode: String,
    val atcCode: String,
    val drugName: String,
)
