package com.safemedi.app.sefemedi.domain.user.controller

import com.safemedi.app.sefemedi.domain.user.dto.TutorialRequest
import com.safemedi.app.sefemedi.domain.user.dto.TutorialResponse
import com.safemedi.app.sefemedi.domain.user.dto.UserProfileResponse
import com.safemedi.app.sefemedi.domain.user.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val userService: UserService,
) {

    @GetMapping("/me")
    fun getMyProfile(
        authentication: Authentication,
    ): UserProfileResponse {
        return userService.getMyProfile(
            authentication.name,
        )
    }

    @PostMapping("/me/tutorial")
    @ResponseStatus(HttpStatus.CREATED)
    fun completeTutorial(
        authentication: Authentication,
        @RequestBody request: TutorialRequest,
    ): TutorialResponse {
        return userService.completeTutorial(
            authentication.name,
            request,
        )
    }
}
