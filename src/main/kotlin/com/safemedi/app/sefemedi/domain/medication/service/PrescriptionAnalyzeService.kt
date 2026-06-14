package com.safemedi.app.sefemedi.domain.medication.service

import com.safemedi.app.sefemedi.domain.drug.repository.DrugIngredientMapRepository
import com.safemedi.app.sefemedi.domain.drug.repository.DrugMasterRepository
import com.safemedi.app.sefemedi.domain.medication.analyzer.MedicationAnalysisTarget
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
    private val drugMasterRepository: DrugMasterRepository,
    analyzers: List<PrescriptionAnalyzer>,
) {
    private val sortedAnalyzers: List<PrescriptionAnalyzer> =
        analyzers.sortedWith(AnnotationAwareOrderComparator.INSTANCE)

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
        val allergies = userAllergyRepository.findAllByUser_IdOrderByCreatedAtAsc(userId)
        val healthProfile = userHealthProfileRepository.findById(userId).orElse(null)
        val drugCodes = request.medications.map { it.drugCode.trim() }
        if (drugCodes.any { it.isBlank() }) {
            throw BusinessException(ErrorCode.INVALID_REQUEST)
        }

        val drugsByCode = drugMasterRepository.findAllById(drugCodes)
            .associateBy { it.drugCode.uppercase() }
        val medications = drugCodes.map { drugCode ->
            val drug = drugsByCode[drugCode.uppercase()] ?: throw BusinessException(ErrorCode.INVALID_REQUEST)
            MedicationAnalysisTarget(
                drugCode = drug.drugCode,
                atcCode = drug.atcCode ?: throw BusinessException(ErrorCode.INVALID_REQUEST),
                drugName = drug.drugName ?: throw BusinessException(ErrorCode.INVALID_REQUEST),
            )
        }
        val ingredientMaps = drugIngredientMapRepository.findAllByDrugCodes(drugCodes)

        val context = PrescriptionContext(
            user = user,
            healthProfile = healthProfile,
            allergies = allergies,
            medications = medications,
            ingredientMaps = ingredientMaps,
        )

        sortedAnalyzers.forEach { it.analyze(context) }

        return context.toResponse()
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
}
