package com.safemedi.app.sefemedi.domain.user.controller

import com.safemedi.app.sefemedi.domain.user.dto.TutorialRequest
import com.safemedi.app.sefemedi.domain.user.dto.TutorialResponse
import com.safemedi.app.sefemedi.domain.user.service.UserService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.springframework.security.core.Authentication

class UserControllerTest {

    private val userService = mock(UserService::class.java)
    private val userController = UserController(userService)

    @Test
    fun `authentication name을 사용자 식별자로 전달한다`() {
        val authentication = mock(Authentication::class.java)
        val request = TutorialRequest(
            birthDate = "1985-03-15",
            gender = "MALE",
        )
        val response = TutorialResponse(
            isTutorialCompleted = true,
        )

        given(authentication.name).willReturn("4903042739")
        given(authentication.principal).willReturn(
            object {
                override fun toString(): String = "wrong-principal-value"
            }
        )
        given(userService.completeTutorial("4903042739", request)).willReturn(response)

        val result = userController.completeTutorial(authentication, request)

        assertEquals(response, result)
        verify(userService).completeTutorial("4903042739", request)
    }
}
