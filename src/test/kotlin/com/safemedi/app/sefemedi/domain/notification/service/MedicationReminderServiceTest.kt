package com.safemedi.app.sefemedi.domain.notification.service

import com.safemedi.app.sefemedi.domain.medication.entity.MedicationRecord
import com.safemedi.app.sefemedi.domain.medication.entity.MedicationStatus
import com.safemedi.app.sefemedi.domain.medication.entity.Prescription
import com.safemedi.app.sefemedi.domain.medication.entity.PrescriptionDrug
import com.safemedi.app.sefemedi.domain.medication.entity.PrescriptionDrugTime
import com.safemedi.app.sefemedi.domain.medication.repository.MedicationRecordRepository
import com.safemedi.app.sefemedi.domain.notification.dto.NotificationCreateCommand
import com.safemedi.app.sefemedi.domain.notification.entity.NotificationTargetType
import com.safemedi.app.sefemedi.domain.notification.entity.NotificationType
import com.safemedi.app.sefemedi.domain.user.entity.User
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockingDetails
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class MedicationReminderServiceTest {

    private lateinit var medicationRecordRepository: MedicationRecordRepository
    private lateinit var notificationCreateService: NotificationCreateService
    private lateinit var medicationReminderService: MedicationReminderService

    private val user = User(
        id = 1L,
        socialId = "kakao-123",
    )

    @BeforeEach
    fun setUp() {
        medicationRecordRepository = mock(MedicationRecordRepository::class.java)
        notificationCreateService = mock(NotificationCreateService::class.java)
        medicationReminderService = MedicationReminderService(
            medicationRecordRepository = medicationRecordRepository,
            notificationCreateService = notificationCreateService,
        )
    }

    @Test
    fun `createDueReminders creates medication reminder notifications for due pending records`() {
        val now = LocalDateTime.of(2026, 6, 27, 9, 0)
        val record = medicationRecord(
            scheduledAt = LocalDateTime.of(2026, 6, 27, 8, 58),
        )

        given(
            medicationRecordRepository.findPendingRecordsScheduledBetween(
                status = MedicationStatus.PENDING,
                startAt = now.minusMinutes(5),
                endAt = now,
            )
        ).willReturn(listOf(record))

        medicationReminderService.createDueReminders(now)

        val command = mockingDetails(notificationCreateService)
            .invocations
            .single()
            .arguments[0] as NotificationCreateCommand
        assertEquals(1L, command.userId)
        assertEquals(NotificationType.MEDICATION_REMINDER, command.type)
        assertEquals("약 복용 시간입니다", command.title)
        assertEquals("Tylenol을 복용할 시간이에요", command.content)
        assertEquals(NotificationTargetType.MEDICATION_RECORD, command.targetType)
        assertEquals(500L, command.targetId)
        assertEquals("MEDICATION_REMINDER:MEDICATION_RECORD:500:1", command.deduplicationKey)
        assertEquals(record.scheduledAt, command.scheduledAt)
    }

    @Test
    fun `createDueReminders does not create notifications when there are no due records`() {
        val now = LocalDateTime.of(2026, 6, 27, 9, 0)

        given(
            medicationRecordRepository.findPendingRecordsScheduledBetween(
                status = MedicationStatus.PENDING,
                startAt = now.minusMinutes(5),
                endAt = now,
            )
        ).willReturn(emptyList())

        medicationReminderService.createDueReminders(now)

        verifyNoInteractions(notificationCreateService)
    }

    @Test
    fun `createDueReminders queries the previous five minute window`() {
        val now = LocalDateTime.of(2026, 6, 27, 9, 0)

        given(
            medicationRecordRepository.findPendingRecordsScheduledBetween(
                status = MedicationStatus.PENDING,
                startAt = now.minusMinutes(5),
                endAt = now,
            )
        ).willReturn(emptyList())

        medicationReminderService.createDueReminders(now)

        verify(medicationRecordRepository).findPendingRecordsScheduledBetween(
            status = MedicationStatus.PENDING,
            startAt = now.minusMinutes(5),
            endAt = now,
        )
    }

    private fun medicationRecord(
        scheduledAt: LocalDateTime,
    ): MedicationRecord {
        val prescription = Prescription(
            id = 10L,
            user = user,
            title = "Prescription",
            startDate = LocalDate.of(2026, 6, 20),
            endDate = LocalDate.of(2026, 6, 30),
        )
        val prescriptionDrug = PrescriptionDrug(
            id = 20L,
            prescription = prescription,
            drugName = "Tylenol",
        )
        val prescriptionDrugTime = PrescriptionDrugTime(
            id = 30L,
            prescriptionDrug = prescriptionDrug,
            takeTime = LocalTime.of(9, 0),
        )

        return MedicationRecord(
            id = 500L,
            user = user,
            prescription = prescription,
            prescriptionDrugTime = prescriptionDrugTime,
            scheduledAt = scheduledAt,
            status = MedicationStatus.PENDING,
        )
    }
}
