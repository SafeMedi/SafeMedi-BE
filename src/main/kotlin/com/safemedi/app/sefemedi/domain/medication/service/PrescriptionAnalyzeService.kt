package com.safemedi.app.sefemedi.domain.medication.service

import com.safemedi.app.sefemedi.domain.drug.repository.DrugIngredientMapRepository
import com.safemedi.app.sefemedi.domain.medication.analyzer.PrescriptionAnalyzer
import com.safemedi.app.sefemedi.domain.medication.analyzer.PrescriptionContext
import com.safemedi.app.sefemedi.domain.medication.dto.AnalyzedMedicationResponse
import com.safemedi.app.sefemedi.domain.medication.dto.MedicationSafetyStatus
import com.safemedi.app.sefemedi.domain.medication.dto.PrescriptionAnalyzeRequest
import com.safemedi.app.sefemedi.domain.medication.dto.PrescriptionAnalyzeResponse
import com.safemedi.app.sefemedi.domain.medication.dto.SafetySummaryResponse
import com.safemedi.app.sefemedi.domain.user.repository.UserAllergyRepository
import com.safemedi.app.sefemedi.domain.user.repository.UserHealthProfileRepository
import com.safemedi.app.sefemedi.domain.user.repository.UserRepository
import com.safemedi.app.sefemedi.global.error.BusinessException
import com.safemedi.app.sefemedi.global.error.ErrorCode
import org.springframework.core.annotation.AnnotationAwareOrderComparator
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PrescriptionAnalyzeService(
    private val userRepository: UserRepository,
    private val userAllergyRepository: UserAllergyRepository,
    private val userHealthProfileRepository: UserHealthProfileRepository,
    private val drugIngredientMapRepository: DrugIngredientMapRepository,
    analyzers: List<PrescriptionAnalyzer>,
) {
    private val firstAnalyzer: PrescriptionAnalyzer =
        analyzers
            .sortedWith(AnnotationAwareOrderComparator.INSTANCE)
            .also {
                it.zipWithNext().forEach { (current, next) -> current.setNext(next) }
            }
            .first()

    @Transactional(readOnly = true)
    fun analyze(
        socialId: String,
        request: PrescriptionAnalyzeRequest,
    ): PrescriptionAnalyzeResponse {
        if (request.medications.isEmpty()) {
            throw BusinessException(ErrorCode.EMPTY_MEDICATIONS)
        }

        val user = userRepository.findBySocialId(socialId)
            ?: throw BusinessException(ErrorCode.INVALID_TOKEN)
        val userId = user.id ?: throw BusinessException(ErrorCode.INVALID_TOKEN)
        val allergies = userAllergyRepository.findByUserId(userId)
        val healthProfile = userHealthProfileRepository.findById(userId).orElse(null)
        val atcCodes = request.medications.map { it.atcCode }.filter { it.isNotBlank() }
        val drugNames = request.medications.map { it.drugName }.filter { it.isNotBlank() }
        val ingredientMaps =
            if (atcCodes.isEmpty() && drugNames.isEmpty()) {
                emptyList()
            } else {
                drugIngredientMapRepository.findAllByMedicationKeys(
                    atcCodes = atcCodes.ifEmpty { listOf(NO_MATCH_KEY) },
                    drugNames = drugNames.ifEmpty { listOf(NO_MATCH_KEY) },
                )
            }

        val context = PrescriptionContext(
            user = user,
            healthProfile = healthProfile,
            allergies = allergies,
            medications = request.medications,
            ingredientMaps = ingredientMaps,
        )

        return firstAnalyzer.analyze(context).toResponse()
    }

    private fun PrescriptionContext.toResponse(): PrescriptionAnalyzeResponse {
        val analyzedResponses = analyzedMedications.map {
            AnalyzedMedicationResponse(
                atcCode = it.medication.atcCode,
                drugName = it.medication.drugName,
                status = it.status,
                efficacy = it.efficacy,
                precautions = it.precautions,
                warnings = it.warnings,
            )
        }

        return PrescriptionAnalyzeResponse(
            safetySummary = SafetySummaryResponse(
                safeCount = analyzedResponses.count { it.status == MedicationSafetyStatus.SAFE },
                warningCount = analyzedResponses.count { it.status == MedicationSafetyStatus.WARNING },
                dangerCount = analyzedResponses.count { it.status == MedicationSafetyStatus.DANGER },
            ),
            analyzedMedications = analyzedResponses,
        )
    }

    private companion object {
        const val NO_MATCH_KEY = "__NO_MATCH__"
    }
}
