package com.safemedi.app.sefemedi.domain.auth.service

import com.safemedi.app.sefemedi.domain.auth.client.SocialLoginVerifier
import com.safemedi.app.sefemedi.domain.auth.dto.LoginResponse
import com.safemedi.app.sefemedi.domain.auth.dto.TokenResponse
import com.safemedi.app.sefemedi.domain.auth.entity.RefreshToken
import com.safemedi.app.sefemedi.domain.auth.repository.RefreshTokenRepository
import com.safemedi.app.sefemedi.domain.user.entity.User
import com.safemedi.app.sefemedi.domain.user.repository.UserRepository
import com.safemedi.app.sefemedi.global.error.BusinessException
import com.safemedi.app.sefemedi.global.error.ErrorCode
import com.safemedi.app.sefemedi.global.jwt.JwtProvider
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.Locale

@Service
class AuthService(

    private val jwtProvider: JwtProvider,
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val socialLoginVerifier: SocialLoginVerifier
) {

    @Transactional
    fun login(
        provider: String,
        accessToken: String
    ): LoginResponse {
        requireSupportedProvider(
            provider,
        )

        val kakaoId =
            socialLoginVerifier.resolveSocialId(
                accessToken = accessToken,
            )

        val issuedTokens =
            issueTokensInternal(
                kakaoId = kakaoId,
            )

        return LoginResponse(
            accessToken = issuedTokens.tokenResponse.accessToken,
            refreshToken = issuedTokens.tokenResponse.refreshToken,
            isTutorialCompleted = issuedTokens.user.isTutorialCompleted,
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

        val user =
            findUserBySocialId(
                kakaoId,
            )

        val savedRefreshToken =
            refreshTokenRepository.findByUser_Id(
                requireUserId(user),
            )
                ?: throw BusinessException(ErrorCode.INVALID_TOKEN)

        if (savedRefreshToken.token != refreshToken) {
            throw BusinessException(ErrorCode.INVALID_TOKEN)
        }

        val accessToken =
            jwtProvider.createAccessToken(kakaoId)

        val newRefreshToken =
            jwtProvider.createRefreshToken(kakaoId)

        saveRefreshToken(
            user = user,
            token = newRefreshToken
        )

        return TokenResponse(
            accessToken = accessToken,
            refreshToken = newRefreshToken
        )
    }

    private fun requireSupportedProvider(
        provider: String
    ) {
        if (provider.trim().lowercase(Locale.ROOT) != KAKAO_PROVIDER) {
            throw BusinessException(ErrorCode.UNSUPPORTED_SOCIAL_LOGIN_PROVIDER)
        }
    }

    private fun issueTokensInternal(
        kakaoId: String
    ): IssuedTokens {
        val user =
            findOrCreateUserBySocialId(
                kakaoId,
            )

        val accessToken =
            jwtProvider.createAccessToken(kakaoId)

        val refreshToken =
            jwtProvider.createRefreshToken(kakaoId)

        saveRefreshToken(
            user = user,
            token = refreshToken
        )

        return IssuedTokens(
            user = user,
            tokenResponse = TokenResponse(
                accessToken = accessToken,
                refreshToken = refreshToken
            )
        )
    }

    private fun saveRefreshToken(
        user: User,
        token: String
    ) {
        val userId =
            requireUserId(
                user,
            )

        val refreshToken =
            refreshTokenRepository.findByUser_Id(
                userId,
            )?.apply {
                this.token = token
            } ?: RefreshToken(
                user = user,
                token = token
            )

        refreshTokenRepository.save(
            refreshToken
        )
    }

    private fun findOrCreateUserBySocialId(
        kakaoId: String
    ): User {
        val existingUser = userRepository.findBySocialIdIncludingDeleted(
            kakaoId
        )

        return when {
            existingUser == null -> userRepository.save(
                User(
                    socialId = kakaoId
                )
            )
            existingUser.deletedAt != null -> throw BusinessException(ErrorCode.USER_ALREADY_WITHDRAWN)
            else -> existingUser
        }
    }

    private fun findUserBySocialId(
        kakaoId: String
    ) = userRepository.findBySocialId(
        kakaoId
    ) ?: throw BusinessException(ErrorCode.INVALID_TOKEN)

    private fun requireUserId(
        user: User
    ): Long {
        return user.id ?: throw BusinessException(ErrorCode.INVALID_TOKEN)
    }

    private data class IssuedTokens(
        val user: User,
        val tokenResponse: TokenResponse,
    )

    private companion object {
        const val KAKAO_PROVIDER = "kakao"
    }
}
