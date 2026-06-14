package com.safemedi.app.sefemedi.domain.auth.repository

import com.safemedi.app.sefemedi.domain.auth.entity.RefreshToken
import org.springframework.data.jpa.repository.JpaRepository

interface RefreshTokenRepository : JpaRepository<RefreshToken, Long> {

    fun findByUser_Id(
        userId: Long
    ): RefreshToken?
}
