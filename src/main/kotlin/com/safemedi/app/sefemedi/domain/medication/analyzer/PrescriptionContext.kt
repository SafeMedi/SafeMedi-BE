package com.safemedi.app.sefemedi.domain.medication.analyzer

import com.safemedi.app.sefemedi.domain.drug.entity.DrugIngredientMap
import com.safemedi.app.sefemedi.domain.medication.dto.MedicationAnalyzeRequest
import com.safemedi.app.sefemedi.domain.medication.dto.MedicationSafetyStatus
import com.safemedi.app.sefemedi.domain.medication.dto.MedicationWarningResponse
import com.safemedi.app.sefemedi.domain.medication.dto.MedicationWarningType
import com.safemedi.app.sefemedi.domain.user.entity.User
import com.safemedi.app.sefemedi.domain.user.entity.UserAllergy
import com.safemedi.app.sefemedi.domain.user.entity.UserHealthProfile

class PrescriptionContext(
    val user: User,
    val healthProfile: UserHealthProfile?,
    val allergies: List<UserAllergy>,
    val medications: List<MedicationAnalyzeRequest>,
    val ingredientMaps: List<DrugIngredientMap>,
) {
    private val mutableAnalyzedMedications =
        medications.map { AnalyzedMedication(it) }

    val analyzedMedications: List<AnalyzedMedication>
        get() = mutableAnalyzedMedications

    fun addPrecaution(
        medication: MedicationAnalyzeRequest,
        message: String,
    ) {
        mutableAnalyzedMedications
            .filter { it.medication === medication }
            .forEach { it.addPrecaution(message) }
    }

    fun addWarning(
        medication: MedicationAnalyzeRequest,
        type: MedicationWarningType,
        message: String,
        status: MedicationSafetyStatus,
    ) {
        mutableAnalyzedMedications
            .filter { it.medication === medication }
            .forEach { it.addWarning(type, message, status) }
    }

    fun ingredientsOf(medication: MedicationAnalyzeRequest): List<String> =
        ingredientMaps
            .filter {
                it.drug.atcCode.equals(medication.atcCode, ignoreCase = true) ||
                    it.drug.drugName.equals(medication.drugName, ignoreCase = true)
            }
            .mapNotNull { it.ingredient.ingredientName }
            .distinct()
}

class AnalyzedMedication(
    val medication: MedicationAnalyzeRequest,
) {
    var status: MedicationSafetyStatus = MedicationSafetyStatus.SAFE
        private set

    val efficacy: String = "정보 없음"
    private val mutablePrecautions = linkedSetOf<String>()
    private val mutableWarnings = mutableListOf<MedicationWarningResponse>()

    val precautions: List<String>
        get() = mutablePrecautions.toList()

    val warnings: List<MedicationWarningResponse>
        get() = mutableWarnings.toList()

    fun addPrecaution(message: String) {
        mutablePrecautions.add(message)
        if (status == MedicationSafetyStatus.SAFE) {
            status = MedicationSafetyStatus.WARNING
        }
    }

    fun addWarning(
        type: MedicationWarningType,
        message: String,
        warningStatus: MedicationSafetyStatus,
    ) {
        mutableWarnings.add(MedicationWarningResponse(type, message))
        if (warningStatus == MedicationSafetyStatus.DANGER || status == MedicationSafetyStatus.SAFE) {
            status = warningStatus
        }
    }
}
