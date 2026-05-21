package com.example.safemedi.domain.user.dto

data class TutorialRequest(

    val birthDate: String,

    val gender: String,

    val height: Int?,

    val weight: Int?,

    val bloodType: String?,

    val diseases: List<String>?,

    val allergies: List<String>?
)