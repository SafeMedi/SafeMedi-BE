package com.safemedi.app.sefemedi.global.security

import com.safemedi.app.sefemedi.domain.auth.service.AuthService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler
import org.springframework.stereotype.Component
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Component
class OAuth2SuccessHandler(

    private val authService: AuthService,

    @param:Value("\${oauth2.success-redirect-url:http://localhost:3000/oauth/callback}")
    private val successRedirectUrl: String

) : SimpleUrlAuthenticationSuccessHandler() {

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {

        val oAuth2User =
            authentication.principal
                    as org.springframework.security.oauth2.core.user.OAuth2User

        val kakaoId = oAuth2User.attributes["id"]
            .toString()

        val tokenResponse =
            authService.issueTokens(
                kakaoId
            )

        val redirectUrl =
            successRedirectUrl +
                    "?accessToken=${encode(tokenResponse.accessToken)}" +
                    "&refreshToken=${encode(tokenResponse.refreshToken)}"

        response.sendRedirect(redirectUrl)
    }

    private fun encode(
        value: String
    ): String {
        return URLEncoder.encode(
            value,
            StandardCharsets.UTF_8
        )
    }
}
