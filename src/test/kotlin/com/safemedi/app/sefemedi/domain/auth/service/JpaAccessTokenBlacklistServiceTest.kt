package com.safemedi.app.sefemedi.domain.auth.service

import com.safemedi.app.sefemedi.domain.auth.entity.AccessTokenBlacklist
import com.safemedi.app.sefemedi.domain.auth.repository.AccessTokenBlacklistRepository
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.BDDMockito.given
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import java.security.MessageDigest
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

class JpaAccessTokenBlacklistServiceTest {

    private val clock =
        Clock.fixed(
            Instant.parse("2026-07-29T00:00:00Z"),
            ZoneOffset.UTC,
        )
    private val accessTokenBlacklistRepository =
        mock(AccessTokenBlacklistRepository::class.java)
    private val service =
        JpaAccessTokenBlacklistService(
            accessTokenBlacklistRepository = accessTokenBlacklistRepository,
            clock = clock,
        )

    @Test
    fun `blacklist stores hashed token with expiration time`() {
        val token = "access-token"
        val expiresAt = Instant.parse("2026-07-29T00:10:00Z")
        val tokenHash = hashToken(token)

        given(accessTokenBlacklistRepository.findByTokenHash(tokenHash)).willReturn(null)

        service.blacklist(
            token = token,
            expiresAt = expiresAt,
        )

        val captor = ArgumentCaptor.forClass(AccessTokenBlacklist::class.java)
        verify(accessTokenBlacklistRepository).save(captor.capture())

        assertEquals(tokenHash, captor.value.tokenHash)
        assertEquals(
            LocalDateTime.ofInstant(expiresAt, clock.zone),
            captor.value.expiresAt,
        )
    }

    @Test
    fun `contains returns true for active blacklisted token`() {
        val token = "access-token"
        val tokenHash = hashToken(token)
        val blacklistedToken =
            AccessTokenBlacklist(
                id = 1L,
                tokenHash = tokenHash,
                expiresAt = LocalDateTime.ofInstant(
                    Instant.parse("2026-07-29T00:10:00Z"),
                    clock.zone,
                ),
            )

        given(accessTokenBlacklistRepository.findByTokenHash(tokenHash)).willReturn(blacklistedToken)

        assertTrue(
            service.contains(token),
        )
    }

    @Test
    fun `contains returns false for expired blacklisted token`() {
        val token = "access-token"
        val tokenHash = hashToken(token)
        val blacklistedToken =
            AccessTokenBlacklist(
                id = 1L,
                tokenHash = tokenHash,
                expiresAt = LocalDateTime.ofInstant(
                    Instant.parse("2026-07-28T23:59:59Z"),
                    clock.zone,
                ),
            )

        given(accessTokenBlacklistRepository.findByTokenHash(tokenHash)).willReturn(blacklistedToken)

        assertFalse(
            service.contains(token),
        )
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
