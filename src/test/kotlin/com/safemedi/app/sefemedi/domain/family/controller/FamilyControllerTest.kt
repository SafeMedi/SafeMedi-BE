package com.safemedi.app.sefemedi.domain.family.controller

import com.safemedi.app.sefemedi.domain.family.dto.FamilyRelationUpdateRequest
import com.safemedi.app.sefemedi.domain.family.dto.FamilyRelationUpdateResponse
import com.safemedi.app.sefemedi.domain.family.service.FamilyRelationUpdateService
import com.safemedi.app.sefemedi.domain.user.dto.MedicalSummaryResponse
import com.safemedi.app.sefemedi.domain.user.service.MedicalSummaryService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.springframework.security.core.Authentication
import java.time.Instant

class FamilyControllerTest {

    private val medicalSummaryService = mock(MedicalSummaryService::class.java)
    private val familyRelationUpdateService = mock(FamilyRelationUpdateService::class.java)
    private val controller = FamilyController(medicalSummaryService, familyRelationUpdateService)

    @Test
    fun `가족 의료정보 조회 요청을 인증 사용자와 가족 ID로 서비스에 전달한다`() {
        val authentication = mock(Authentication::class.java)
        val response = MedicalSummaryResponse(
            name = "김영희",
            birthDate = null,
            gender = null,
            height = null,
            weight = null,
            bloodType = null,
            rhType = null,
            diseases = emptyList(),
            allergies = emptyList(),
            activeMedications = emptyList(),
        )

        given(authentication.name).willReturn("kakao-123")
        given(medicalSummaryService.getFamilyMedicalSummary("kakao-123", 9L)).willReturn(response)

        val result = controller.getFamilyMedicalSummary(authentication, 9L)

        assertEquals(response, result)
        verify(medicalSummaryService).getFamilyMedicalSummary("kakao-123", 9L)
    }

    @Test
    fun `호칭 수정 요청을 인증 사용자와 가족 ID로 서비스에 전달한다`() {
        val authentication = mock(Authentication::class.java)
        val request = FamilyRelationUpdateRequest(relation = "어머니")
        val response = FamilyRelationUpdateResponse(
            familyId = 12L,
            name = "김영희",
            relation = "어머니",
            updatedAt = Instant.parse("2026-07-15T06:10:00Z"),
        )

        given(authentication.name).willReturn("kakao-123")
        given(familyRelationUpdateService.update("kakao-123", 12L, request)).willReturn(response)

        val result = controller.updateRelation(authentication, 12L, request)

        assertEquals(response, result)
        verify(familyRelationUpdateService).update("kakao-123", 12L, request)
    }
}
