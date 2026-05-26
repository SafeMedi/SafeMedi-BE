package com.safemedi.app.sefemedi.domain.medication.service

import com.safemedi.app.sefemedi.domain.drug.entity.DrugIngredientMap
import com.safemedi.app.sefemedi.domain.drug.entity.DrugMaster
import com.safemedi.app.sefemedi.domain.drug.entity.DurInteraction
import com.safemedi.app.sefemedi.domain.drug.entity.IngredientMaster
import com.safemedi.app.sefemedi.domain.drug.repository.DrugIngredientMapRepository
import com.safemedi.app.sefemedi.domain.drug.repository.DurAgeRepository
import com.safemedi.app.sefemedi.domain.drug.repository.DurElderlyRepository
import com.safemedi.app.sefemedi.domain.drug.repository.DurInteractionRepository
import com.safemedi.app.sefemedi.domain.drug.repository.DurPregnancyRepository
import com.safemedi.app.sefemedi.domain.medication.analyzer.AtcCodeAllergyAnalyzerImpl
import com.safemedi.app.sefemedi.domain.medication.analyzer.DurSafetyAnalyzerImpl
import com.safemedi.app.sefemedi.domain.medication.analyzer.IngredientAllergyAnalyzerImpl
import com.safemedi.app.sefemedi.domain.medication.dto.MedicationAnalyzeRequest
import com.safemedi.app.sefemedi.domain.medication.dto.MedicationSafetyStatus
import com.safemedi.app.sefemedi.domain.medication.dto.MedicationWarningType
import com.safemedi.app.sefemedi.domain.medication.dto.PrescriptionAnalyzeRequest
import com.safemedi.app.sefemedi.domain.user.entity.AllergyType
import com.safemedi.app.sefemedi.domain.user.entity.User
import com.safemedi.app.sefemedi.domain.user.entity.UserAllergy
import com.safemedi.app.sefemedi.domain.user.repository.UserAllergyRepository
import com.safemedi.app.sefemedi.domain.user.repository.UserHealthProfileRepository
import com.safemedi.app.sefemedi.domain.user.repository.UserRepository
import com.safemedi.app.sefemedi.global.error.BusinessException
import com.safemedi.app.sefemedi.global.error.ErrorCode
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.mock
import java.time.LocalDate
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class PrescriptionAnalyzeServiceTest {
    private lateinit var userRepository: UserRepository
    private lateinit var userAllergyRepository: UserAllergyRepository
    private lateinit var userHealthProfileRepository: UserHealthProfileRepository
    private lateinit var drugIngredientMapRepository: DrugIngredientMapRepository
    private lateinit var durElderlyRepository: DurElderlyRepository
    private lateinit var durAgeRepository: DurAgeRepository
    private lateinit var durPregnancyRepository: DurPregnancyRepository
    private lateinit var durInteractionRepository: DurInteractionRepository
    private lateinit var service: PrescriptionAnalyzeService

    private val user = User(
        id = 1L,
        socialId = "kakao-123",
    )

    @BeforeEach
    fun setUp() {
        userRepository = mock(UserRepository::class.java)
        userAllergyRepository = mock(UserAllergyRepository::class.java)
        userHealthProfileRepository = mock(UserHealthProfileRepository::class.java)
        drugIngredientMapRepository = mock(DrugIngredientMapRepository::class.java)
        durElderlyRepository = mock(DurElderlyRepository::class.java)
        durAgeRepository = mock(DurAgeRepository::class.java)
        durPregnancyRepository = mock(DurPregnancyRepository::class.java)
        durInteractionRepository = mock(DurInteractionRepository::class.java)

        val durAnalyzer = DurSafetyAnalyzerImpl(
            durElderlyRepository = durElderlyRepository,
            durAgeRepository = durAgeRepository,
            durPregnancyRepository = durPregnancyRepository,
            durInteractionRepository = durInteractionRepository,
        )

        service = PrescriptionAnalyzeService(
            userRepository = userRepository,
            userAllergyRepository = userAllergyRepository,
            userHealthProfileRepository = userHealthProfileRepository,
            drugIngredientMapRepository = drugIngredientMapRepository,
            analyzers = listOf(
                durAnalyzer,
                AtcCodeAllergyAnalyzerImpl(),
                IngredientAllergyAnalyzerImpl(),
            ),
        )
    }

    @Test
    fun `예외 처리`() {
        val exception = assertFailsWith<BusinessException> {
            service.analyze(
                socialId = "kakao-123",
                request = PrescriptionAnalyzeRequest(medications = emptyList()),
            )
        }

        assertEquals(ErrorCode.EMPTY_MEDICATIONS, exception.errorCode)
        assertEquals("MED_002", exception.errorCode.code)
    }

    @Test
    fun `복합 경고 누적`() {
        val medications = listOf(
            MedicationAnalyzeRequest(atcCode = "N02BE01", drugName = "타이레놀 500mg"),
            MedicationAnalyzeRequest(atcCode = "C08CA01", drugName = "암로디핀정 5mg"),
        )
        val ingredientMap = DrugIngredientMap(
            drug = DrugMaster(
                drugCode = "D001",
                drugName = "타이레놀 500mg",
                atcCode = "N02BE01",
            ),
            ingredient = IngredientMaster(
                ingredientCode = "I001",
                ingredientName = "아세트아미노펜",
            ),
        )
        val allergies = listOf(
            UserAllergy(
                user = user,
                allergyType = AllergyType.INGREDIENT,
                allergyValue = "I001",
                allergyName = "아세트아미노펜",
            ),
            UserAllergy(
                user = user,
                allergyType = AllergyType.ATC_GROUP,
                allergyValue = "C08",
                allergyName = "칼슘채널차단제",
            ),
        )
        val interaction = DurInteraction(
            drugNameA = "타이레놀 500mg",
            drugNameB = "암로디핀정 5mg",
            noticeNumber = "DUR-001",
            noticeDate = LocalDate.of(2026, 1, 1),
            warningMessage = "병용 금기 조합입니다.",
        )

        given(userRepository.findBySocialId("kakao-123")).willReturn(user)
        given(userAllergyRepository.findByUserId(1L)).willReturn(allergies)
        given(userHealthProfileRepository.findById(1L)).willReturn(Optional.empty())
        given(
            drugIngredientMapRepository.findAllByMedicationKeys(
                listOf("N02BE01", "C08CA01"),
                listOf("타이레놀 500mg", "암로디핀정 5mg"),
            )
        ).willReturn(listOf(ingredientMap))
        given(durAgeRepository.findByDrugNameIn(listOf("타이레놀 500mg", "암로디핀정 5mg"))).willReturn(emptyList())
        given(durPregnancyRepository.findByDrugNameIn(listOf("타이레놀 500mg", "암로디핀정 5mg"))).willReturn(emptyList())
        given(durInteractionRepository.findInteractions(listOf("타이레놀 500mg", "암로디핀정 5mg")))
            .willReturn(listOf(interaction))

        val response = service.analyze(
            socialId = "kakao-123",
            request = PrescriptionAnalyzeRequest(medications = medications),
        )

        assertEquals(0, response.safetySummary.safeCount)
        assertEquals(0, response.safetySummary.warningCount)
        assertEquals(2, response.safetySummary.dangerCount)

        val tylenol = response.analyzedMedications.first { it.drugName == "타이레놀 500mg" }
        assertEquals(MedicationSafetyStatus.DANGER, tylenol.status)
        assertTrue(tylenol.warnings.any { it.type == MedicationWarningType.ALLERGY })
        assertTrue(tylenol.warnings.any { it.type == MedicationWarningType.DUR_INTERACTION })

        val amlodipine = response.analyzedMedications.first { it.drugName == "암로디핀정 5mg" }
        assertEquals(MedicationSafetyStatus.DANGER, amlodipine.status)
        assertTrue(amlodipine.warnings.any { it.type == MedicationWarningType.ALLERGY })
        assertTrue(amlodipine.warnings.any { it.type == MedicationWarningType.DUR_INTERACTION })
    }
}
