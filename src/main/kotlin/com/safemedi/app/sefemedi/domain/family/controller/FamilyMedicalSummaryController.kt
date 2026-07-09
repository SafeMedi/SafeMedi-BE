package com.safemedi.app.sefemedi.domain.family.controller

import com.safemedi.app.sefemedi.domain.user.dto.MedicalSummaryResponse
import com.safemedi.app.sefemedi.domain.user.service.MedicalSummaryService
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/families")
class FamilyMedicalSummaryController(
    private val medicalSummaryService: MedicalSummaryService,
) {

    @GetMapping("/{familyId}/medical-summary")
    fun getFamilyMedicalSummary(
        authentication: Authentication,
        @PathVariable familyId: Long,
    ): MedicalSummaryResponse {
        return medicalSummaryService.getFamilyMedicalSummary(
            socialId = authentication.name,
            familyId = familyId,
        )
    }
}
