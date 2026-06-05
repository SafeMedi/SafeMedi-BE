package com.safemedi.app.sefemedi.global.jwt

import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*

@Component
class JwtProvider(

    @param:Value("\${jwt.secret}")
    private val secretKey: String
) {

    private val key =
        Keys.hmacShaKeyFor(
            secretKey.toByteArray()
        )

    private val accessTokenExpirationMillis =
        1000L * 60 * 60 * 24

    private val refreshTokenExpirationMillis =
        1000L * 60 * 60 * 24 * 14

    fun createAccessToken(
        kakaoId: String
    ): String {
        return createToken(
            kakaoId,
            accessTokenExpirationMillis
        )
    }

    fun createRefreshToken(
        kakaoId: String
    ): String {
        return createToken(
            kakaoId,
            refreshTokenExpirationMillis
        )
    }

    fun getKakaoId(
        token: String
    ): String {
        return getClaims(token).subject
    }

    fun validateToken(
        token: String
    ): Boolean {
        return try {
            getClaims(token)
            true
        } catch (e: JwtException) {
            false
        } catch (e: IllegalArgumentException) {
            false
        }
    }

    private fun createToken(
        kakaoId: String,
        expirationMillis: Long
    ): String {
        val now = Date()

        val expiredDate =
            Date(
                now.time + expirationMillis
            )

        return Jwts.builder()
            .subject(kakaoId)
            .issuedAt(now)
            .expiration(expiredDate)
            .signWith(key)
            .compact()
    }

    private fun getClaims(
        token: String
    ): Claims {
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
    }
}
