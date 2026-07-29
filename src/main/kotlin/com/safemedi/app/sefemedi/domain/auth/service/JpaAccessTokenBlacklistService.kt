package com.safemedi.app.sefemedi.domain.auth.service

import com.safemedi.app.sefemedi.domain.auth.entity.AccessTokenBlacklist
import com.safemedi.app.sefemedi.domain.auth.repository.AccessTokenBlacklistRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.MessageDigest
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime

@Service
class JpaAccessTokenBlacklistService(
    private val accessTokenBlacklistRepository: AccessTokenBlacklistRepository,
    private val clock: Clock,
) : AccessTokenBlacklistService {

    @Transactional(readOnly = true)
    override fun contains(
        token: String,
    ): Boolean {
        val tokenHash = hashToken(token)
        val blacklistedToken =
            accessTokenBlacklistRepository.findByTokenHash(tokenHash)
                ?: return false

        val now = LocalDateTime.now(clock)
        return blacklistedToken.expiresAt.isAfter(now)
    }

    @Transactional
    override fun blacklist(
        token: String,
        expiresAt: Instant,
    ) {
        val now = LocalDateTime.now(clock)
        val expiresAtDateTime = LocalDateTime.ofInstant(expiresAt, clock.zone)

        if (!expiresAtDateTime.isAfter(now)) {
            return
        }

        val tokenHash = hashToken(token)

        val blacklistedToken =
            accessTokenBlacklistRepository.findByTokenHash(tokenHash)
                ?.apply {
                    this.expiresAt = expiresAtDateTime
                } ?: AccessTokenBlacklist(
                tokenHash = tokenHash,
                expiresAt = expiresAtDateTime,
            )

        accessTokenBlacklistRepository.save(blacklistedToken)
    }

    private fun hashToken(
        token: String,
    ): String {
        val digest =
            MessageDigest.getInstance("SHA-256")
                .digest(token.toByteArray(Charsets.UTF_8))

        return digest.joinToString(separator = "") {
            "%02x".format(it.toInt() and 0xff)
        }
    }
}
