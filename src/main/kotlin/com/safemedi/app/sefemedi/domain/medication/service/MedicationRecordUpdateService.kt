package com.safemedi.app.sefemedi.domain.medication.service

import com.safemedi.app.sefemedi.domain.medication.dto.MedicationRecordUpdateRequest
import com.safemedi.app.sefemedi.domain.medication.dto.MedicationRecordUpdateResponse
import com.safemedi.app.sefemedi.domain.medication.entity.MedicationRecord
import com.safemedi.app.sefemedi.domain.medication.entity.MedicationStatus
import com.safemedi.app.sefemedi.domain.medication.repository.MedicationRecordRepository
import com.safemedi.app.sefemedi.domain.notification.dto.NotificationCreateCommand
import com.safemedi.app.sefemedi.domain.notification.entity.NotificationTargetType
import com.safemedi.app.sefemedi.domain.notification.entity.NotificationType
import com.safemedi.app.sefemedi.domain.notification.service.NotificationCreateService
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
    private val notificationCreateService: NotificationCreateService,
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
        if (request.status == MedicationStatus.SUCCESS) {
            createMedicationCompletedNotification(record)
        }

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

    private fun createMedicationCompletedNotification(
        record: MedicationRecord,
    ) {
        val recordId = record.id ?: throw BusinessException(ErrorCode.INTERNAL_SERVER_ERROR)
        val userId = record.user.id ?: throw BusinessException(ErrorCode.INTERNAL_SERVER_ERROR)
        val drugName = record.prescriptionDrugTime.prescriptionDrug.drugName

        notificationCreateService.create(
            NotificationCreateCommand(
                userId = userId,
                type = NotificationType.MEDICATION_COMPLETED,
                title = "복약 완료",
                content = "${drugName} 복용을 완료했어요",
                targetType = NotificationTargetType.MEDICATION_RECORD,
                targetId = recordId,
                deduplicationKey = "MEDICATION_COMPLETED:MEDICATION_RECORD:$recordId:$userId",
                scheduledAt = record.takenAt,
            )
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
