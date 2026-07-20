package com.safemedi.app.sefemedi.domain.auth.service

import com.safemedi.app.sefemedi.domain.auth.client.SocialLoginVerifier
import com.safemedi.app.sefemedi.domain.auth.entity.AccessTokenBlacklist
import com.safemedi.app.sefemedi.domain.auth.entity.RefreshToken
import com.safemedi.app.sefemedi.domain.auth.dto.LogoutRequest
import com.safemedi.app.sefemedi.domain.auth.repository.AccessTokenBlacklistRepository
import com.safemedi.app.sefemedi.domain.auth.repository.RefreshTokenRepository
import com.safemedi.app.sefemedi.domain.user.entity.User
import com.safemedi.app.sefemedi.domain.user.entity.UserDevice
import com.safemedi.app.sefemedi.domain.user.repository.UserRepository
import com.safemedi.app.sefemedi.domain.user.repository.UserDeviceRepository
import com.safemedi.app.sefemedi.global.error.BusinessException
import com.safemedi.app.sefemedi.global.error.ErrorCode
import com.safemedi.app.sefemedi.global.jwt.JwtProvider
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.given
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import java.util.Date

class AuthServiceTest {

    private lateinit var jwtProvider: JwtProvider
    private lateinit var userRepository: UserRepository
    private lateinit var refreshTokenRepository: RefreshTokenRepository
    private lateinit var accessTokenBlacklistRepository: AccessTokenBlacklistRepository
    private lateinit var socialLoginVerifier: SocialLoginVerifier
    private lateinit var userDeviceRepository: UserDeviceRepository
    private lateinit var authService: AuthService

    @BeforeEach
    fun setUp() {
        jwtProvider = mock(JwtProvider::class.java)
        userRepository = mock(UserRepository::class.java)
        refreshTokenRepository = mock(RefreshTokenRepository::class.java)
        accessTokenBlacklistRepository = mock(AccessTokenBlacklistRepository::class.java)
        userDeviceRepository = mock(UserDeviceRepository::class.java)
        socialLoginVerifier = object : SocialLoginVerifier {
            override fun resolveSocialId(accessToken: String): String {
                return "4903042739"
            }
        }

        authService = AuthService(
            jwtProvider = jwtProvider,
            userRepository = userRepository,
            refreshTokenRepository = refreshTokenRepository,
            accessTokenBlacklistRepository = accessTokenBlacklistRepository,
            socialLoginVerifier = socialLoginVerifier,
            userDeviceRepository = userDeviceRepository,
        )
    }

    private fun givenValidLogoutAccessToken(
        socialId: String = "4903042739",
        accessToken: String = "access-token",
    ) {
        given(jwtProvider.validateToken(accessToken)).willReturn(true)
        given(jwtProvider.getKakaoId(accessToken)).willReturn(socialId)
        given(jwtProvider.getExpiration(accessToken)).willReturn(Date(System.currentTimeMillis() + 60_000))
        given(accessTokenBlacklistRepository.existsByToken(accessToken)).willReturn(false)
        given(accessTokenBlacklistRepository.save(any(AccessTokenBlacklist::class.java))).willReturn(
            AccessTokenBlacklist(
                token = accessToken,
                expiresAt = java.time.LocalDateTime.now(),
            )
        )
    }

    @Test
    fun `login creates a user and returns JWT tokens`() {
        val savedUser = User(
            id = 1L,
            socialId = "4903042739",
            isTutorialCompleted = false,
        )

        given(userRepository.findBySocialIdIncludingDeleted("4903042739")).willReturn(null)
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

    @Test
    fun `logout deactivates current user's active device token and discards tokens`() {
        val user = User(
            id = 1L,
            socialId = "4903042739",
        )
        val userDevice = UserDevice(
            id = 10L,
            user = user,
            deviceToken = "device-token",
            deviceType = "ANDROID",
            isActive = true,
        )

        given(userRepository.findBySocialId("4903042739")).willReturn(user)
        given(userDeviceRepository.findByDeviceToken("device-token")).willReturn(userDevice)
        givenValidLogoutAccessToken()

        val response = authService.logout(
            socialId = "4903042739",
            accessToken = "access-token",
            request = LogoutRequest(
                deviceToken = "device-token",
            ),
        )

        assertEquals("로그아웃이 성공적으로 진행되었습니다.", response.message)
        assertEquals(false, userDevice.isActive)
        verify(refreshTokenRepository).deleteByUserId(1L)
        verify(accessTokenBlacklistRepository).save(any(AccessTokenBlacklist::class.java))
    }

    @Test
    fun `logout succeeds when the device token is already inactive`() {
        val user = User(
            id = 1L,
            socialId = "4903042739",
        )
        val userDevice = UserDevice(
            id = 10L,
            user = user,
            deviceToken = "device-token",
            deviceType = "ANDROID",
            isActive = false,
        )

        given(userRepository.findBySocialId("4903042739")).willReturn(user)
        given(userDeviceRepository.findByDeviceToken("device-token")).willReturn(userDevice)
        givenValidLogoutAccessToken()

        val response = authService.logout(
            socialId = "4903042739",
            accessToken = "access-token",
            request = LogoutRequest(
                deviceToken = "device-token",
            ),
        )

        assertEquals("로그아웃이 성공적으로 진행되었습니다.", response.message)
        assertEquals(false, userDevice.isActive)
    }

    @Test
    fun `logout throws LOGOUT_DEVICE_TOKEN_REQUIRED when device token is blank`() {
        val exception = assertThrows(BusinessException::class.java) {
            authService.logout(
                socialId = "4903042739",
                accessToken = "access-token",
                request = LogoutRequest(
                    deviceToken = " ",
                ),
            )
        }

        assertEquals(ErrorCode.LOGOUT_DEVICE_TOKEN_REQUIRED, exception.errorCode)
    }

    @Test
    fun `logout throws LOGOUT_DEVICE_TOKEN_TOO_LONG when device token exceeds length limit`() {
        val exception = assertThrows(BusinessException::class.java) {
            authService.logout(
                socialId = "4903042739",
                accessToken = "access-token",
                request = LogoutRequest(
                    deviceToken = "a".repeat(513),
                ),
            )
        }

        assertEquals(ErrorCode.LOGOUT_DEVICE_TOKEN_TOO_LONG, exception.errorCode)
    }

    @Test
    fun `logout throws LOGOUT_DEVICE_TOKEN_NOT_FOUND when token does not exist`() {
        val user = User(
            id = 1L,
            socialId = "4903042739",
        )

        given(userRepository.findBySocialId("4903042739")).willReturn(user)
        given(userDeviceRepository.findByDeviceToken("device-token")).willReturn(null)
        givenValidLogoutAccessToken()

        val exception = assertThrows(BusinessException::class.java) {
            authService.logout(
                socialId = "4903042739",
                accessToken = "access-token",
                request = LogoutRequest(
                    deviceToken = "device-token",
                ),
            )
        }

        assertEquals(ErrorCode.LOGOUT_DEVICE_TOKEN_NOT_FOUND, exception.errorCode)
    }

    @Test
    fun `logout throws LOGOUT_DEVICE_TOKEN_ACCESS_DENIED when token belongs to another user`() {
        val currentUser = User(
            id = 1L,
            socialId = "4903042739",
        )
        val otherUser = User(
            id = 2L,
            socialId = "other",
        )
        val userDevice = UserDevice(
            id = 10L,
            user = otherUser,
            deviceToken = "device-token",
            deviceType = "ANDROID",
            isActive = true,
        )

        given(userRepository.findBySocialId("4903042739")).willReturn(currentUser)
        given(userDeviceRepository.findByDeviceToken("device-token")).willReturn(userDevice)
        givenValidLogoutAccessToken()

        val exception = assertThrows(BusinessException::class.java) {
            authService.logout(
                socialId = "4903042739",
                accessToken = "access-token",
                request = LogoutRequest(
                    deviceToken = "device-token",
                ),
            )
        }

        assertEquals(ErrorCode.LOGOUT_DEVICE_TOKEN_ACCESS_DENIED, exception.errorCode)
    }

    @Test
    fun `login reactivates withdrawn user and returns JWT tokens`() {
        val withdrawnUser = User(
            id = 1L,
            socialId = "4903042739",
            deletedAt = java.time.LocalDateTime.now(),
        )

        given(userRepository.findBySocialIdIncludingDeleted("4903042739")).willReturn(withdrawnUser)
        given(userRepository.save(withdrawnUser)).willReturn(withdrawnUser)
        given(jwtProvider.createAccessToken("4903042739")).willReturn("app-access-token")
        given(jwtProvider.createRefreshToken("4903042739")).willReturn("app-refresh-token")
        given(refreshTokenRepository.findByUser_Id(1L)).willReturn(null)
        given(refreshTokenRepository.save(any(RefreshToken::class.java))).willReturn(
            RefreshToken(
                user = withdrawnUser,
                token = "app-refresh-token",
            )
        )

        val response = authService.login(
            provider = "kakao",
            accessToken = "kakao-access-token",
        )

        assertEquals("app-access-token", response.accessToken)
        assertEquals("app-refresh-token", response.refreshToken)
        assertNull(withdrawnUser.deletedAt)
        assertEquals(false, response.isTutorialCompleted)
        verify(userRepository).save(withdrawnUser)
    }
}
