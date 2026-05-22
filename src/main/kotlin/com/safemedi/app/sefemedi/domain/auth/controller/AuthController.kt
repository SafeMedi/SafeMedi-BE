package com.safemedi.app.sefemedi.domain.auth.controller

import com.safemedi.app.sefemedi.domain.auth.dto.TokenReissueRequest
import com.safemedi.app.sefemedi.domain.auth.dto.TokenResponse
import com.safemedi.app.sefemedi.domain.auth.service.AuthService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthController(

    private val authService: AuthService
) {

    @PostMapping("/reissue")
    fun reissue(
        @RequestBody request: TokenReissueRequest
    ): TokenResponse {
        return authService.reissue(
            request.refreshToken
        )
    }
}
