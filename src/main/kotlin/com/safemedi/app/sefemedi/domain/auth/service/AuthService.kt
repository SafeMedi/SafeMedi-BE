package com.safemedi.app.sefemedi.domain.auth.service

import com.safemedi.app.sefemedi.domain.auth.dto.TokenResponse
import com.safemedi.app.sefemedi.domain.user.repository.UserRepository
import com.safemedi.app.sefemedi.global.error.BusinessException
import com.safemedi.app.sefemedi.global.error.ErrorCode
import com.safemedi.app.sefemedi.global.jwt.JwtProvider
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.concurrent.ConcurrentHashMap

@Service
class AuthService(

    private val jwtProvider: JwtProvider,
    private val userRepository: UserRepository
) {

    private val refreshTokenStore =
        ConcurrentHashMap<String, String>()

    @Transactional
    fun issueTokens(
        kakaoId: String
    ): TokenResponse {
        userRepository.findBySocialId(
            kakaoId
        ) ?: throw BusinessException(ErrorCode.INVALID_TOKEN)

        val accessToken =
            jwtProvider.createAccessToken(kakaoId)

        val refreshToken =
            jwtProvider.createRefreshToken(kakaoId)

        saveRefreshToken(
            kakaoId,
            refreshToken
        )

        return TokenResponse(
            accessToken = accessToken,
            refreshToken = refreshToken
        )
    }

    @Transactional
    fun reissue(
        refreshToken: String
    ): TokenResponse {
        if (!jwtProvider.validateToken(refreshToken)) {
            throw BusinessException(ErrorCode.INVALID_TOKEN)
        }

        val kakaoId =
            jwtProvider.getKakaoId(refreshToken)

        val savedRefreshToken =
            refreshTokenStore[kakaoId]

        if (savedRefreshToken != refreshToken) {
            throw BusinessException(ErrorCode.INVALID_TOKEN)
        }

        val accessToken =
            jwtProvider.createAccessToken(kakaoId)

        val newRefreshToken =
            jwtProvider.createRefreshToken(kakaoId)

        saveRefreshToken(
            kakaoId,
            newRefreshToken
        )

        return TokenResponse(
            accessToken = accessToken,
            refreshToken = newRefreshToken
        )
    }

    private fun saveRefreshToken(
        kakaoId: String,
        token: String
    ) {
        refreshTokenStore[kakaoId] = token
    }
}
