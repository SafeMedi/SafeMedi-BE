package com.safemedi.app.sefemedi.domain.auth.service

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.security.MessageDigest
import java.time.Duration
import java.time.Instant
import java.util.Base64

@Service
class RedisAccessTokenBlacklistService(

    private val stringRedisTemplate: StringRedisTemplate,
) : AccessTokenBlacklistService {

    override fun contains(
        token: String,
    ): Boolean {
        return stringRedisTemplate.hasKey(
            blacklistKey(token),
        ) == true
    }

    override fun blacklist(
        token: String,
        expiresAt: Instant,
    ) {
        val ttl =
            Duration.between(
                Instant.now(),
                expiresAt,
            )

        if (ttl.isZero || ttl.isNegative) {
            return
        }

        stringRedisTemplate.opsForValue().set(
            blacklistKey(token),
            BLACKLISTED_VALUE,
            ttl,
        )
    }

    private fun blacklistKey(
        token: String,
    ): String {
        val tokenHash =
            MessageDigest.getInstance("SHA-256")
                .digest(token.toByteArray(Charsets.UTF_8))

        return "$KEY_PREFIX${
            Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(tokenHash)
        }"
    }

    private companion object {
        const val KEY_PREFIX = "auth:access-token:blacklist:"
        const val BLACKLISTED_VALUE = "1"
    }
}
