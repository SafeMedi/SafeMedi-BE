package com.safemedi.app.sefemedi.domain.medication.service

import com.safemedi.app.sefemedi.domain.medication.entity.MedicationRecord
import com.safemedi.app.sefemedi.domain.medication.entity.MedicationStatus
import com.safemedi.app.sefemedi.domain.medication.entity.Prescription
import com.safemedi.app.sefemedi.domain.medication.entity.PrescriptionDrug
import com.safemedi.app.sefemedi.domain.medication.entity.PrescriptionDrugTime
import com.safemedi.app.sefemedi.domain.medication.repository.MedicationRecordRepository
import com.safemedi.app.sefemedi.domain.user.entity.User
import com.safemedi.app.sefemedi.domain.user.repository.UserRepository
import com.safemedi.app.sefemedi.global.error.BusinessException
import com.safemedi.app.sefemedi.global.error.ErrorCode
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.mock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TodayMedicationScheduleServiceTest {
    private lateinit var userRepository: UserRepository
    private lateinit var medicationRecordRepository: MedicationRecordRepository
    private lateinit var service: TodayMedicationScheduleService

    private val user = User(
        id = 1L,
        socialId = "kakao-123",
    )

    @BeforeEach
    fun setUp() {
        userRepository = mock(UserRepository::class.java)
        medicationRecordRepository = mock(MedicationRecordRepository::class.java)

        service = TodayMedicationScheduleService(
            userRepository = userRepository,
            medicationRecordRepository = medicationRecordRepository,
        )
    }

    @Test
    fun `오늘 스케줄 조회`() {
        val today = LocalDate.now(ZoneId.of("Asia/Seoul"))
        val prescription = Prescription(
            id = 25L,
            user = user,
            title = "Kidney prescription",
            startDate = today,
            endDate = today,
        )
        val tylenolRecord = medicationRecord(
            id = 101L,
            user = user,
            prescription = prescription,
            drugName = "Tylenol 500mg",
            scheduledAt = today.atTime(8, 0),
            status = MedicationStatus.SUCCESS,
        )
        val omeprazoleRecord = medicationRecord(
            id = 102L,
            user = user,
            prescription = prescription,
            drugName = "Omeprazole 20mg",
            scheduledAt = today.atTime(8, 0),
            status = MedicationStatus.SUCCESS,
        )
        val eveningRecord = medicationRecord(
            id = 103L,
            user = user,
            prescription = prescription,
            drugName = "Tylenol 500mg",
            scheduledAt = today.atTime(18, 0),
            status = MedicationStatus.PENDING,
        )

        given(userRepository.findBySocialId("kakao-123")).willReturn(user)
        given(
            medicationRecordRepository.findTodaySchedules(
                userId = 1L,
                startAt = today.atStartOfDay(),
                endAt = today.plusDays(1).atStartOfDay(),
            )
        ).willReturn(listOf(tylenolRecord, omeprazoleRecord, eveningRecord))

        val response = service.findTodaySchedules("kakao-123")

        assertEquals(today, response.date)
        assertEquals(1, response.summary.completedCount)
        assertEquals(2, response.summary.totalCount)
        assertEquals(50, response.summary.completionRate)

        val morningSchedule = response.schedules.first()
        assertEquals("08:00", morningSchedule.takeTime)
        assertEquals("SUCCESS", morningSchedule.recordStatus)
        assertEquals("SUCCESS", morningSchedule.displayStatus)
        assertEquals(25L, morningSchedule.prescriptionId)
        assertEquals(2, morningSchedule.drugCount)
        assertEquals(listOf("Tylenol 500mg", "Omeprazole 20mg"), morningSchedule.drugNames)
        assertEquals(listOf(101L, 102L), morningSchedule.recordIds)
    }

    @Test
    fun `사용자 없음`() {
        given(userRepository.findBySocialId("kakao-404")).willReturn(null)

        val exception = assertFailsWith<BusinessException> {
            service.findTodaySchedules("kakao-404")
        }

        assertEquals(ErrorCode.INVALID_TOKEN, exception.errorCode)
    }

    private fun medicationRecord(
        id: Long,
        user: User,
        prescription: Prescription,
        drugName: String,
        scheduledAt: LocalDateTime,
        status: MedicationStatus,
    ): MedicationRecord {
        val prescriptionDrug = PrescriptionDrug(
            id = id,
            prescription = prescription,
            drugName = drugName,
            atcCode = "A01AA01",
        )
        val prescriptionDrugTime = PrescriptionDrugTime(
            id = id,
            prescriptionDrug = prescriptionDrug,
            takeTime = LocalTime.from(scheduledAt),
        )

        return MedicationRecord(
            id = id,
            user = user,
            prescription = prescription,
            prescriptionDrugTime = prescriptionDrugTime,
            scheduledAt = scheduledAt,
            status = status,
        )
    }
}
