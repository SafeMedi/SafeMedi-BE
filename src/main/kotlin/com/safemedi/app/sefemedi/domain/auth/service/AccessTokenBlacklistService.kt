package com.safemedi.app.sefemedi.domain.auth.service

import java.util.Date

interface AccessTokenBlacklistService {

    fun contains(
        token: String,
    ): Boolean

    fun blacklist(
        token: String,
        expiresAt: Date,
    )
}
