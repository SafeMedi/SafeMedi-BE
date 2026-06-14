package com.safemedi.app.sefemedi.domain.user.repository

import com.safemedi.app.sefemedi.domain.user.entity.UserAllergy
import org.springframework.data.jpa.repository.JpaRepository

interface UserAllergyRepository : JpaRepository<UserAllergy, Long> {

    fun findAllByUser_IdOrderByCreatedAtAsc(
        userId: Long,
    ): List<UserAllergy>
}
