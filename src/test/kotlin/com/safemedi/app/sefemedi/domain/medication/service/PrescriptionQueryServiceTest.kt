package com.safemedi.app.sefemedi.domain.medication.service

import com.safemedi.app.sefemedi.domain.medication.entity.Prescription
import com.safemedi.app.sefemedi.domain.medication.repository.PrescriptionDrugCountProjection
import com.safemedi.app.sefemedi.domain.medication.repository.PrescriptionDrugRepository
import com.safemedi.app.sefemedi.domain.medication.repository.PrescriptionRepository
import com.safemedi.app.sefemedi.domain.user.entity.User
import com.safemedi.app.sefemedi.domain.user.repository.UserRepository
import com.safemedi.app.sefemedi.global.error.BusinessException
import com.safemedi.app.sefemedi.global.error.ErrorCode
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.mock
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.test.util.ReflectionTestUtils
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PrescriptionQueryServiceTest {
    private lateinit var userRepository: UserRepository
    private lateinit var prescriptionRepository: PrescriptionRepository
    private lateinit var prescriptionDrugRepository: PrescriptionDrugRepository
    private lateinit var service: PrescriptionQueryService

    private val user = User(
        id = 1L,
        socialId = "kakao-123",
    )

    @BeforeEach
    fun setUp() {
        userRepository = mock(UserRepository::class.java)
        prescriptionRepository = mock(PrescriptionRepository::class.java)
        prescriptionDrugRepository = mock(PrescriptionDrugRepository::class.java)

        service = PrescriptionQueryService(
            userRepository = userRepository,
            prescriptionRepository = prescriptionRepository,
            prescriptionDrugRepository = prescriptionDrugRepository,
        )
    }

    @Test
    fun `처방전 목록 조회`() {
        val prescription = Prescription(
            id = 25L,
            user = user,
            title = "Kidney prescription",
            hasAllergyConflict = true,
            isDoctorApproved = true,
            startDate = LocalDate.of(2026, 6, 1),
            endDate = LocalDate.of(2026, 6, 7),
        )
        ReflectionTestUtils.setField(
            prescription,
            "createdAt",
            LocalDateTime.of(2026, 6, 5, 10, 0),
        )

        given(userRepository.findBySocialId("kakao-123")).willReturn(user)
        given(
            prescriptionRepository.findByUserIdOrderByCreatedAtDescIdDesc(
                userId = 1L,
                pageable = PageRequest.of(0, 10),
            )
        ).willReturn(PageImpl(listOf(prescription), PageRequest.of(0, 10), 1))
        given(prescriptionDrugRepository.countByPrescriptionIds(listOf(25L)))
            .willReturn(
                listOf(
                    PrescriptionDrugCount(
                        prescriptionId = 25L,
                        drugCount = 3L,
                    )
                )
            )

        val response = service.findPrescriptions(
            socialId = "kakao-123",
            page = 0,
            size = 10,
        )

        assertEquals(true, response.isLast)
        assertEquals(1, response.content.size)
        assertEquals(25L, response.content.single().prescriptionId)
        assertEquals("Kidney prescription", response.content.single().title)
        assertEquals(LocalDate.of(2026, 6, 5), response.content.single().createdAt)
        assertEquals(3, response.content.single().drugCount)
        assertEquals(true, response.content.single().hasAllergyConflict)
    }

    @Test
    fun `페이징 오류`() {
        val exception = assertFailsWith<BusinessException> {
            service.findPrescriptions(
                socialId = "kakao-123",
                page = -1,
                size = 10,
            )
        }

        assertEquals(ErrorCode.INVALID_PAGE_REQUEST, exception.errorCode)
    }

    private data class PrescriptionDrugCount(
        override val prescriptionId: Long,
        override val drugCount: Long,
    ) : PrescriptionDrugCountProjection
}
