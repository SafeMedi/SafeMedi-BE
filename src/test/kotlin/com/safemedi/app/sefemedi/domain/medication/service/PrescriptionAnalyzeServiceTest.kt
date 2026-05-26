package com.safemedi.app.sefemedi.domain.medication.service

import com.safemedi.app.sefemedi.domain.drug.entity.DrugIngredientMap
import com.safemedi.app.sefemedi.domain.drug.entity.DrugMaster
import com.safemedi.app.sefemedi.domain.drug.entity.DurInteraction
import com.safemedi.app.sefemedi.domain.drug.entity.IngredientMaster
import com.safemedi.app.sefemedi.domain.drug.repository.DrugIngredientMapRepository
import com.safemedi.app.sefemedi.domain.drug.repository.DrugMasterRepository
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
    private lateinit var drugMasterRepository: DrugMasterRepository
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
        drugMasterRepository = mock(DrugMasterRepository::class.java)
        durElderlyRepository = mock(DurElderlyRepository::class.java)
        durAgeRepository = mock(DurAgeRepository::class.java)
        durPregnancyRepository = mock(DurPregnancyRepository::class.java)
        durInteractionRepository = mock(DurInteractionRepository::class.java)

        service = PrescriptionAnalyzeService(
            userRepository = userRepository,
            userAllergyRepository = userAllergyRepository,
            userHealthProfileRepository = userHealthProfileRepository,
            drugIngredientMapRepository = drugIngredientMapRepository,
            drugMasterRepository = drugMasterRepository,
            analyzers = listOf(
                DurSafetyAnalyzerImpl(
                    durElderlyRepository = durElderlyRepository,
                    durAgeRepository = durAgeRepository,
                    durPregnancyRepository = durPregnancyRepository,
                    durInteractionRepository = durInteractionRepository,
                ),
                AtcCodeAllergyAnalyzerImpl(),
                IngredientAllergyAnalyzerImpl(),
            ),
        )
    }

    @Test
    fun `에러 테스트`() {
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
    fun `분석 테스트`() {
        val medications = listOf(
            MedicationAnalyzeRequest(drugCode = "D001"),
            MedicationAnalyzeRequest(drugCode = "D002"),
        )
        val tylenolDrug = DrugMaster(
            drugCode = "D001",
            drugName = "Tylenol 500mg",
            atcCode = "N02BE01",
        )
        val amlodipineDrug = DrugMaster(
            drugCode = "D002",
            drugName = "Amlodipine 5mg",
            atcCode = "C08CA01",
        )
        val ingredientMap = DrugIngredientMap(
            drug = tylenolDrug,
            ingredient = IngredientMaster(
                ingredientCode = "I001",
                ingredientName = "Acetaminophen",
            ),
        )
        val allergies = listOf(
            UserAllergy(
                user = user,
                allergyType = AllergyType.INGREDIENT,
                allergyValue = "I001",
                allergyName = "Acetaminophen",
            ),
            UserAllergy(
                user = user,
                allergyType = AllergyType.ATC_GROUP,
                allergyValue = "C08",
                allergyName = "Calcium channel blocker",
            ),
        )
        val drugNames = listOf("Tylenol 500mg", "Amlodipine 5mg")
        val interaction = DurInteraction(
            drugNameA = "Tylenol 500mg",
            drugNameB = "Amlodipine 5mg",
            noticeNumber = "DUR-001",
            noticeDate = LocalDate.of(2026, 1, 1),
            warningMessage = "Contraindicated interaction.",
        )

        given(userRepository.findBySocialId("kakao-123")).willReturn(user)
        given(userAllergyRepository.findByUserId(1L)).willReturn(allergies)
        given(userHealthProfileRepository.findById(1L)).willReturn(Optional.empty())
        given(drugMasterRepository.findAllById(listOf("D001", "D002"))).willReturn(listOf(tylenolDrug, amlodipineDrug))
        given(drugIngredientMapRepository.findAllByDrugCodes(listOf("D001", "D002"))).willReturn(listOf(ingredientMap))
        given(durAgeRepository.findByDrugNameIn(drugNames)).willReturn(emptyList())
        given(durPregnancyRepository.findByDrugNameIn(drugNames)).willReturn(emptyList())
        given(durInteractionRepository.findInteractions(drugNames)).willReturn(listOf(interaction))

        val response = service.analyze(
            socialId = "kakao-123",
            request = PrescriptionAnalyzeRequest(medications = medications),
        )

        assertEquals(0, response.safetySummary.safeCount)
        assertEquals(0, response.safetySummary.warningCount)
        assertEquals(2, response.safetySummary.dangerCount)

        val tylenol = response.analyzedMedications.first { it.drugName == "Tylenol 500mg" }
        assertEquals("N02BE01", tylenol.atcCode)
        assertEquals(MedicationSafetyStatus.DANGER, tylenol.status)
        assertTrue(tylenol.warnings.any { it.type == MedicationWarningType.ALLERGY })
        assertTrue(tylenol.warnings.any { it.type == MedicationWarningType.DUR_INTERACTION })

        val amlodipine = response.analyzedMedications.first { it.drugName == "Amlodipine 5mg" }
        assertEquals("C08CA01", amlodipine.atcCode)
        assertEquals(MedicationSafetyStatus.DANGER, amlodipine.status)
        assertTrue(amlodipine.warnings.any { it.type == MedicationWarningType.ALLERGY })
        assertTrue(amlodipine.warnings.any { it.type == MedicationWarningType.DUR_INTERACTION })
    }
}
