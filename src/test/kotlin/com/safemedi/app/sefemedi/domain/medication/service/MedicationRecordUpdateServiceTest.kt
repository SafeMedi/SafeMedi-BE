package com.safemedi.app.sefemedi.domain.medication.service

import com.safemedi.app.sefemedi.domain.medication.dto.MedicationRecordUpdateRequest
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
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class MedicationRecordUpdateServiceTest {
    private lateinit var userRepository: UserRepository
    private lateinit var medicationRecordRepository: MedicationRecordRepository
    private lateinit var service: MedicationRecordUpdateService

    private val user = User(
        id = 1L,
        socialId = "kakao-123",
    )

    @BeforeEach
    fun setUp() {
        userRepository = mock(UserRepository::class.java)
        medicationRecordRepository = mock(MedicationRecordRepository::class.java)

        service = MedicationRecordUpdateService(
            userRepository = userRepository,
            medicationRecordRepository = medicationRecordRepository,
        )
    }

    @Test
    fun `복약 기록을 성공 처리한다`() {
        val record = medicationRecord(status = MedicationStatus.PENDING)

        given(userRepository.findBySocialId("kakao-123")).willReturn(user)
        given(medicationRecordRepository.findActiveById(500L)).willReturn(record)

        val response = service.update(
            socialId = "kakao-123",
            recordId = 500L,
            request = MedicationRecordUpdateRequest(
                status = MedicationStatus.SUCCESS,
            ),
        )

        assertEquals(500L, response.recordId)
        assertEquals(10L, response.prescriptionId)
        assertEquals(MedicationStatus.SUCCESS.name, response.status)
        assertNotNull(response.takenAt)
        assertEquals(MedicationStatus.SUCCESS, record.status)
        assertNotNull(record.takenAt)
    }

    @Test
    fun `복약 기록을 건너뜀 처리한다`() {
        val record = medicationRecord(status = MedicationStatus.PENDING)

        given(userRepository.findBySocialId("kakao-123")).willReturn(user)
        given(medicationRecordRepository.findActiveById(500L)).willReturn(record)

        val response = service.update(
            socialId = "kakao-123",
            recordId = 500L,
            request = MedicationRecordUpdateRequest(
                status = MedicationStatus.SKIP,
            ),
        )

        assertEquals(MedicationStatus.SKIP.name, response.status)
        assertNull(response.takenAt)
        assertEquals(MedicationStatus.SKIP, record.status)
        assertNull(record.takenAt)
    }

    @Test
    fun `존재하지 않는 복약 기록이면 오류`() {
        given(userRepository.findBySocialId("kakao-123")).willReturn(user)
        given(medicationRecordRepository.findActiveById(999L)).willReturn(null)

        val exception = assertFailsWith<BusinessException> {
            service.update(
                socialId = "kakao-123",
                recordId = 999L,
                request = MedicationRecordUpdateRequest(
                    status = MedicationStatus.SUCCESS,
                ),
            )
        }

        assertEquals(ErrorCode.MEDICATION_RECORD_NOT_FOUND, exception.errorCode)
    }

    @Test
    fun `이미 처리된 복약 기록이면 오류`() {
        val record = medicationRecord(status = MedicationStatus.SUCCESS)

        given(userRepository.findBySocialId("kakao-123")).willReturn(user)
        given(medicationRecordRepository.findActiveById(500L)).willReturn(record)

        val exception = assertFailsWith<BusinessException> {
            service.update(
                socialId = "kakao-123",
                recordId = 500L,
                request = MedicationRecordUpdateRequest(
                    status = MedicationStatus.SKIP,
                ),
            )
        }

        assertEquals(ErrorCode.MEDICATION_RECORD_ALREADY_PROCESSED, exception.errorCode)
    }

    @Test
    fun `이미 처리된 복약 기록을 대기 상태로 되돌린다`() {
        val record = medicationRecord(status = MedicationStatus.SUCCESS).apply {
            updateStatus(
                status = MedicationStatus.SUCCESS,
                takenAt = LocalDateTime.of(2026, 4, 6, 8, 5),
            )
        }

        given(userRepository.findBySocialId("kakao-123")).willReturn(user)
        given(medicationRecordRepository.findActiveById(500L)).willReturn(record)

        val response = service.update(
            socialId = "kakao-123",
            recordId = 500L,
            request = MedicationRecordUpdateRequest(
                status = MedicationStatus.PENDING,
            ),
        )

        assertEquals(MedicationStatus.PENDING.name, response.status)
        assertNull(response.takenAt)
        assertEquals(MedicationStatus.PENDING, record.status)
        assertNull(record.takenAt)
    }

    private fun medicationRecord(
        status: MedicationStatus,
    ): MedicationRecord {
        val prescription = Prescription(
            id = 10L,
            user = user,
            title = "Prescription",
            startDate = LocalDate.of(2026, 4, 1),
            endDate = LocalDate.of(2026, 4, 7),
        )
        val prescriptionDrug = PrescriptionDrug(
            id = 20L,
            prescription = prescription,
            drugName = "Tylenol",
        )
        val prescriptionDrugTime = PrescriptionDrugTime(
            id = 30L,
            prescriptionDrug = prescriptionDrug,
            takeTime = LocalTime.of(8, 0),
        )

        return MedicationRecord(
            id = 500L,
            user = user,
            prescription = prescription,
            prescriptionDrugTime = prescriptionDrugTime,
            scheduledAt = LocalDateTime.of(2026, 4, 6, 8, 0),
            status = status,
        )
    }
}
