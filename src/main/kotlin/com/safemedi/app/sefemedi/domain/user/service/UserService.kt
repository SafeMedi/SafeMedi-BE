package com.safemedi.app.sefemedi.domain.user.service

import com.safemedi.app.sefemedi.domain.drug.repository.DiseaseMasterRepository
import com.safemedi.app.sefemedi.domain.user.dto.TutorialRequest
import com.safemedi.app.sefemedi.domain.user.dto.TutorialResponse
import com.safemedi.app.sefemedi.domain.user.entity.AllergyType
import com.safemedi.app.sefemedi.domain.user.entity.BloodType
import com.safemedi.app.sefemedi.domain.user.entity.Gender
import com.safemedi.app.sefemedi.domain.user.entity.RhType
import com.safemedi.app.sefemedi.domain.user.entity.UserAllergy
import com.safemedi.app.sefemedi.domain.user.entity.UserDiseaseMap
import com.safemedi.app.sefemedi.domain.user.entity.UserHealthProfile
import com.safemedi.app.sefemedi.domain.user.repository.UserAllergyRepository
import com.safemedi.app.sefemedi.domain.user.repository.UserDiseaseMapRepository
import com.safemedi.app.sefemedi.domain.user.repository.UserHealthProfileRepository
import com.safemedi.app.sefemedi.domain.user.repository.UserRepository
import com.safemedi.app.sefemedi.global.error.BusinessException
import com.safemedi.app.sefemedi.global.error.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.format.DateTimeParseException

@Service
class UserService(
    private val userRepository: UserRepository,
    private val userHealthProfileRepository: UserHealthProfileRepository,
    private val userDiseaseMapRepository: UserDiseaseMapRepository,
    private val userAllergyRepository: UserAllergyRepository,
    private val diseaseMasterRepository: DiseaseMasterRepository,
) {

    @Transactional
    fun completeTutorial(
        socialId: String,
        request: TutorialRequest,
    ): TutorialResponse {
        val user = userRepository.findBySocialId(socialId)
            ?: throw BusinessException(ErrorCode.INVALID_TOKEN)

        if (user.isTutorialCompleted) {
            throw BusinessException(ErrorCode.TUTORIAL_ALREADY_COMPLETED)
        }

        val userId = user.id
            ?: throw BusinessException(ErrorCode.INVALID_TOKEN)

        val profile: UserHealthProfile = userHealthProfileRepository.findById(userId)
            .orElseGet {
                UserHealthProfile(
                    userId = userId,
                    user = user,
                )
            }

        profile.birthDate = parseBirthDate(request.birthDate)
        profile.gender = parseEnum<Gender>(request.gender)
        profile.height = request.height
        profile.weight = request.weight
        profile.bloodType = request.bloodType?.let { parseEnum<BloodType>(it) }
        profile.rhType = request.rhType?.let { parseEnum<RhType>(it) } ?: RhType.PLUS

        userHealthProfileRepository.save(profile)

        request.diseaseCodes.forEach { diseaseCode ->
            val disease = diseaseMasterRepository.findById(diseaseCode)
                .orElseThrow { BusinessException(ErrorCode.INVALID_DISEASE_CODE) }

            userDiseaseMapRepository.save(
                UserDiseaseMap(
                    user = user,
                    disease = disease,
                )
            )
        }

        request.allergies.forEach { allergy ->
            userAllergyRepository.save(
                UserAllergy(
                    user = user,
                    allergyType = parseEnum<AllergyType>(allergy.type),
                    allergyValue = allergy.value,
                    allergyName = allergy.name,
                )
            )
        }

        user.isTutorialCompleted = true

        return TutorialResponse(
            isTutorialCompleted = true,
        )
    }

    private fun parseBirthDate(birthDate: String): LocalDate {
        return try {
            LocalDate.parse(birthDate)
        } catch (_: DateTimeParseException) {
            throw BusinessException(ErrorCode.INVALID_ENUM_VALUE)
        }
    }

    private inline fun <reified T : Enum<T>> parseEnum(value: String): T {
        return try {
            enumValueOf<T>(value)
        } catch (_: IllegalArgumentException) {
            throw BusinessException(ErrorCode.INVALID_ENUM_VALUE)
        }
    }
}
