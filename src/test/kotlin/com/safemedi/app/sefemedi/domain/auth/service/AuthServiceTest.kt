package com.safemedi.app.sefemedi.domain.auth.service

import com.safemedi.app.sefemedi.domain.auth.client.SocialLoginVerifier
import com.safemedi.app.sefemedi.domain.auth.entity.RefreshToken
import com.safemedi.app.sefemedi.domain.auth.repository.RefreshTokenRepository
import com.safemedi.app.sefemedi.domain.user.entity.User
import com.safemedi.app.sefemedi.domain.user.repository.UserRepository
import com.safemedi.app.sefemedi.global.error.BusinessException
import com.safemedi.app.sefemedi.global.error.ErrorCode
import com.safemedi.app.sefemedi.global.jwt.JwtProvider
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.given
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

class AuthServiceTest {

    private lateinit var jwtProvider: JwtProvider
    private lateinit var userRepository: UserRepository
    private lateinit var refreshTokenRepository: RefreshTokenRepository
    private lateinit var authService: AuthService

    @BeforeEach
    fun setUp() {
        jwtProvider = mock(JwtProvider::class.java)
        userRepository = mock(UserRepository::class.java)
        refreshTokenRepository = mock(RefreshTokenRepository::class.java)

        authService = AuthService(
            jwtProvider = jwtProvider,
            userRepository = userRepository,
            refreshTokenRepository = refreshTokenRepository,
        ).apply {
            socialLoginVerifier = object : SocialLoginVerifier {
                override fun resolveSocialId(accessToken: String): String {
                    return "4903042739"
                }
            }
        }
    }

    @Test
    fun `login creates a user and returns JWT tokens`() {
        val savedUser = User(
            id = 1L,
            socialId = "4903042739",
            isTutorialCompleted = false,
        )

        given(userRepository.findBySocialId("4903042739")).willReturn(null)
        given(userRepository.save(any(User::class.java))).willReturn(savedUser)
        given(jwtProvider.createAccessToken("4903042739")).willReturn("app-access-token")
        given(jwtProvider.createRefreshToken("4903042739")).willReturn("app-refresh-token")
        given(refreshTokenRepository.findByUser_Id(1L)).willReturn(null)
        given(refreshTokenRepository.save(any(RefreshToken::class.java))).willReturn(
            RefreshToken(
                user = savedUser,
                token = "app-refresh-token",
            )
        )

        val response = authService.login(
            provider = "kakao",
            accessToken = "kakao-access-token",
        )

        assertEquals("app-access-token", response.accessToken)
        assertEquals("app-refresh-token", response.refreshToken)
        assertEquals(false, response.isTutorialCompleted)
        verify(userRepository).save(any(User::class.java))
    }

    @Test
    fun `login rejects unsupported provider`() {
        val exception = assertThrows(BusinessException::class.java) {
            authService.login(
                provider = "naver",
                accessToken = "kakao-access-token",
            )
        }

        assertEquals(ErrorCode.UNSUPPORTED_SOCIAL_LOGIN_PROVIDER, exception.errorCode)
    }

    @Test
    fun `reissue rotates access and refresh tokens`() {
        val user = User(
            id = 1L,
            socialId = "4903042739",
        )
        val refreshToken = "refresh-token"
        val storedRefreshToken = RefreshToken(
            user = user,
            token = refreshToken,
        )

        given(jwtProvider.validateToken(refreshToken)).willReturn(true)
        given(jwtProvider.getKakaoId(refreshToken)).willReturn("4903042739")
        given(userRepository.findBySocialId("4903042739")).willReturn(user)
        given(refreshTokenRepository.findByUser_Id(1L)).willReturn(storedRefreshToken)
        given(jwtProvider.createAccessToken("4903042739")).willReturn("new-access-token")
        given(jwtProvider.createRefreshToken("4903042739")).willReturn("new-refresh-token")
        given(refreshTokenRepository.save(any(RefreshToken::class.java))).willReturn(storedRefreshToken)

        val response = authService.reissue(refreshToken)

        assertEquals("new-access-token", response.accessToken)
        assertEquals("new-refresh-token", response.refreshToken)
        assertEquals("new-refresh-token", storedRefreshToken.token)
    }
}
