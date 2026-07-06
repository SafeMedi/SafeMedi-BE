package com.safemedi.app.sefemedi.domain.family.controller

import com.safemedi.app.sefemedi.domain.user.dto.MedicalSummaryResponse
import com.safemedi.app.sefemedi.domain.user.service.MedicalSummaryService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.springframework.security.core.Authentication

class FamilyMedicalSummaryControllerTest {

    private val medicalSummaryService = mock(MedicalSummaryService::class.java)
    private val controller = FamilyMedicalSummaryController(medicalSummaryService)

    @Test
    fun `getFamilyMedicalSummary passes authentication name and familyId to service`() {
        val authentication = mock(Authentication::class.java)
        val response = MedicalSummaryResponse(
            name = "정민수",
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
}
