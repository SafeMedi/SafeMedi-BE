package com.safemedi.app.sefemedi.domain.family.service

import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import java.util.HexFormat

data class FamilyInvitationToken(
    val rawValue: String,
    val hash: String,
)

@Component
class FamilyInvitationTokenGenerator {
    private val secureRandom = SecureRandom()

    fun generate(): FamilyInvitationToken {
        val bytes = ByteArray(TOKEN_BYTE_LENGTH).also(secureRandom::nextBytes)
        val rawValue = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
        val hash = MessageDigest.getInstance("SHA-256")
            .digest(rawValue.toByteArray(StandardCharsets.UTF_8))
            .let(HexFormat.of()::formatHex)

        return FamilyInvitationToken(rawValue = rawValue, hash = hash)
    }

    private companion object {
        const val TOKEN_BYTE_LENGTH = 32
    }
}
