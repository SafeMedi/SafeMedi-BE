package com.safemedi.app.sefemedi.domain.notification.service

import com.safemedi.app.sefemedi.domain.medication.entity.MedicationRecord
import com.safemedi.app.sefemedi.domain.medication.entity.MedicationStatus
import com.safemedi.app.sefemedi.domain.medication.repository.MedicationRecordRepository
import com.safemedi.app.sefemedi.domain.notification.dto.NotificationCreateCommand
import com.safemedi.app.sefemedi.domain.notification.entity.NotificationTargetType
import com.safemedi.app.sefemedi.domain.notification.entity.NotificationType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class MedicationReminderService(
    private val medicationRecordRepository: MedicationRecordRepository,
    private val notificationCreateService: NotificationCreateService,
) {

    @Transactional
    fun createDueReminders(
        now: LocalDateTime,
    ) {
        val records = medicationRecordRepository.findPendingRecordsScheduledBetween(
            status = MedicationStatus.PENDING,
            startAt = now.minusMinutes(REMINDER_LOOKBACK_MINUTES),
            endAt = now,
        )

        records.forEach { record ->
            notificationCreateService.create(
                NotificationCreateCommand(
                    userId = record.requireUserId(),
                    type = NotificationType.MEDICATION_REMINDER,
                    title = "약 복용 시간입니다",
                    content = "${record.drugName()}을 복용할 시간이에요",
                    targetType = NotificationTargetType.MEDICATION_RECORD,
                    targetId = record.requireRecordId(),
                    deduplicationKey = "MEDICATION_REMINDER:MEDICATION_RECORD:${record.requireRecordId()}:${record.requireUserId()}",
                    scheduledAt = record.scheduledAt,
                )
            )
        }
    }

    private fun MedicationRecord.requireRecordId(): Long {
        return requireNotNull(id)
    }

    private fun MedicationRecord.requireUserId(): Long {
        return requireNotNull(user.id)
    }

    private fun MedicationRecord.drugName(): String {
        return prescriptionDrugTime.prescriptionDrug.drugName
    }

    private companion object {
        const val REMINDER_LOOKBACK_MINUTES = 5L
    }
}
