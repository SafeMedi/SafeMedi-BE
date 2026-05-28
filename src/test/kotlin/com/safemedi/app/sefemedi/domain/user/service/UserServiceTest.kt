package com.safemedi.app.sefemedi.domain.user.service

import com.safemedi.app.sefemedi.domain.drug.entity.DiseaseMaster
import com.safemedi.app.sefemedi.domain.drug.repository.DiseaseMasterRepository
import com.safemedi.app.sefemedi.domain.user.dto.TutorialRequest
import com.safemedi.app.sefemedi.domain.user.dto.TutorialAllergyRequest
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
import org.mockito.Mockito.anyList
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
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
    fun `튜토리얼 정보를 등록하면 완료 상태로 변경된다`() {
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
            diseaseCodes = listOf("D001", "D002"),
            allergies = listOf(
                TutorialAllergyRequest(
                    type = "FOOD",
                    value = "Peanut",
                    name = "Peanut",
                )
            ),
        )

        given(userRepository.findBySocialId("4903042739")).willReturn(user)
        given(userHealthProfileRepository.findById(1L)).willReturn(Optional.empty())
        given(diseaseMasterRepository.findAllById(listOf("D001", "D002"))).willReturn(
            listOf(
                DiseaseMaster(
                    diseaseCode = "D001",
                    diseaseName = "Disease 1",
                ),
                DiseaseMaster(
                    diseaseCode = "D002",
                    diseaseName = "Disease 2",
                ),
            )
        )

        val response = userService.completeTutorial("4903042739", request)

        assertEquals(true, response.isTutorialCompleted)
        assertEquals(true, user.isTutorialCompleted)
        verify(diseaseMasterRepository).findAllById(listOf("D001", "D002"))
        verify(userDiseaseMapRepository).saveAll(anyList())
        verify(userAllergyRepository).saveAll(anyList())
    }

    @Test
    fun `생년월일 형식이 올바르지 않으면 INVALID_DATE_FORMAT 예외를 던진다`() {
        val user = User(
            id = 1L,
            socialId = "4903042739",
            isTutorialCompleted = false,
        )
        val request = TutorialRequest(
            birthDate = "1985/03/15",
            gender = "MALE",
        )

        given(userRepository.findBySocialId("4903042739")).willReturn(user)
        given(userHealthProfileRepository.findById(1L)).willReturn(Optional.empty())

        val exception = assertThrows(BusinessException::class.java) {
            userService.completeTutorial("4903042739", request)
        }

        assertEquals(ErrorCode.INVALID_DATE_FORMAT, exception.errorCode)
    }

    @Test
    fun `이미 튜토리얼을 완료한 유저면 예외를 던진다`() {
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
