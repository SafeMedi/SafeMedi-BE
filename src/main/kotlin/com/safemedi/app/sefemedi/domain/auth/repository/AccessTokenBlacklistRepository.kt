package com.safemedi.app.sefemedi.domain.auth.repository

import com.safemedi.app.sefemedi.domain.auth.entity.AccessTokenBlacklist
import org.springframework.data.jpa.repository.JpaRepository

interface AccessTokenBlacklistRepository : JpaRepository<AccessTokenBlacklist, Long> {

    fun findByTokenHash(
        tokenHash: String,
    ): AccessTokenBlacklist?
}
