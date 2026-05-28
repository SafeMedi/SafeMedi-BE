package com.safemedi.app.sefemedi.domain.user.service

import com.safemedi.app.sefemedi.domain.drug.repository.DiseaseMasterRepository
import com.safemedi.app.sefemedi.domain.user.dto.TutorialRequest
import com.safemedi.app.sefemedi.domain.user.entity.User
import com.safemedi.app.sefemedi.domain.user.repository.UserAllergyRepository
import com.safemedi.app.sefemedi.domain.user.repository.UserDiseaseMapRepository
import com.safemedi.app.sefemedi.domain.user.repository.UserHealthProfileRepository
import com.safemedi.app.sefemedi.domain.user.repository.UserRepository
import com.safemedi.app.sefemedi.global.error.BusinessException
import com.safemedi.app.sefemedi.global.error.ErrorCode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.mock
import java.util.Optional

class UserServiceTest {

    private lateinit var userRepository: UserRepository
    private lateinit var userHealthProfileRepository: UserHealthProfileRepository
    private lateinit var userDiseaseMapRepository: UserDiseaseMapRepository
    private lateinit var userAllergyRepository: UserAllergyRepository
    private lateinit var diseaseMasterRepository: DiseaseMasterRepository
    private lateinit var userService: UserService

    @BeforeEach
    fun setUp() {
        userRepository = mock(UserRepository::class.java)
        userHealthProfileRepository = mock(UserHealthProfileRepository::class.java)
        userDiseaseMapRepository = mock(UserDiseaseMapRepository::class.java)
        userAllergyRepository = mock(UserAllergyRepository::class.java)
        diseaseMasterRepository = mock(DiseaseMasterRepository::class.java)

        userService = UserService(
            userRepository = userRepository,
            userHealthProfileRepository = userHealthProfileRepository,
            userDiseaseMapRepository = userDiseaseMapRepository,
            userAllergyRepository = userAllergyRepository,
            diseaseMasterRepository = diseaseMasterRepository,
        )
    }

    @Test
    fun `튜토리얼 정보를 등록하고 완료 상태로 변경한다`() {
        val user = User(
            id = 1L,
            socialId = "4903042739",
            isTutorialCompleted = false,
        )
        val request = TutorialRequest(
            birthDate = "1985-03-15",
            gender = "MALE",
            height = 180,
            weight = 75,
            bloodType = "O",
            rhType = "PLUS",
        )

        given(userRepository.findBySocialId("4903042739")).willReturn(user)
        given(userHealthProfileRepository.findById(1L)).willReturn(Optional.empty())

        val response = userService.completeTutorial("4903042739", request)

        assertEquals(true, response.isTutorialCompleted)
        assertEquals(true, user.isTutorialCompleted)
    }

    @Test
    fun `이미 완료된 튜토리얼이면 TUT_001 예외를 던진다`() {
        val user = User(
            id = 1L,
            socialId = "4903042739",
            isTutorialCompleted = true,
        )
        val request = TutorialRequest(
            birthDate = "1985-03-15",
            gender = "MALE",
        )

        given(userRepository.findBySocialId("4903042739")).willReturn(user)

        val exception = assertThrows(BusinessException::class.java) {
            userService.completeTutorial("4903042739", request)
        }

        assertEquals(ErrorCode.TUTORIAL_ALREADY_COMPLETED, exception.errorCode)
    }
}
