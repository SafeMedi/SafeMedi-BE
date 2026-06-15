package com.safemedi.app.sefemedi.domain.medication.service

import com.safemedi.app.sefemedi.domain.medication.dto.MedicationRecordUpdateRequest
import com.safemedi.app.sefemedi.domain.medication.dto.MedicationRecordUpdateResponse
import com.safemedi.app.sefemedi.domain.medication.entity.MedicationRecord
import com.safemedi.app.sefemedi.domain.medication.entity.MedicationStatus
import com.safemedi.app.sefemedi.domain.medication.repository.MedicationRecordRepository
import com.safemedi.app.sefemedi.domain.user.repository.UserRepository
import com.safemedi.app.sefemedi.global.error.BusinessException
import com.safemedi.app.sefemedi.global.error.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.ZoneId

@Service
class MedicationRecordUpdateService(
    private val userRepository: UserRepository,
    private val medicationRecordRepository: MedicationRecordRepository,
) {
    @Transactional
    fun update(
        socialId: String,
        recordId: Long,
        request: MedicationRecordUpdateRequest,
    ): MedicationRecordUpdateResponse {
        validateRequestedStatus(request.status)

        val user = userRepository.findBySocialId(socialId)
            ?: throw BusinessException(ErrorCode.INVALID_TOKEN)
        val userId = user.id ?: throw BusinessException(ErrorCode.INVALID_TOKEN)
        val record = medicationRecordRepository.findActiveById(recordId)
            ?: throw BusinessException(ErrorCode.MEDICATION_RECORD_NOT_FOUND)

        if (record.user.id != userId) {
            throw BusinessException(ErrorCode.MEDICATION_RECORD_NOT_FOUND)
        }
        if (request.status != MedicationStatus.PENDING && record.status != MedicationStatus.PENDING) {
            throw BusinessException(ErrorCode.MEDICATION_RECORD_ALREADY_PROCESSED)
        }

        val takenAt = when (request.status) {
            MedicationStatus.SUCCESS -> LocalDateTime.now(SERVICE_ZONE_ID)
            MedicationStatus.SKIP,
            MedicationStatus.PENDING -> null
            MedicationStatus.FAIL -> throw BusinessException(ErrorCode.INVALID_ENUM_VALUE)
        }
        record.updateStatus(
            status = request.status,
            takenAt = takenAt,
        )

        return record.toResponse()
    }

    private fun validateRequestedStatus(status: MedicationStatus) {
        if (status !in ALLOWED_REQUEST_STATUSES) {
            throw BusinessException(ErrorCode.INVALID_ENUM_VALUE)
        }
    }

    private fun MedicationRecord.toResponse(): MedicationRecordUpdateResponse {
        return MedicationRecordUpdateResponse(
            recordId = id ?: throw BusinessException(ErrorCode.INTERNAL_SERVER_ERROR),
            prescriptionId = prescription.id ?: throw BusinessException(ErrorCode.INTERNAL_SERVER_ERROR),
            scheduledAt = scheduledAt,
            takenAt = takenAt,
            status = status.name,
        )
    }

    private companion object {
        val SERVICE_ZONE_ID: ZoneId = ZoneId.of("Asia/Seoul")
        val ALLOWED_REQUEST_STATUSES = setOf(
            MedicationStatus.SUCCESS,
            MedicationStatus.SKIP,
            MedicationStatus.PENDING,
        )
    }
}
