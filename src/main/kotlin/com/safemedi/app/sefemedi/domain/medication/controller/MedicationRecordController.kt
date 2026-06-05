package com.safemedi.app.sefemedi.domain.medication.controller

import com.safemedi.app.sefemedi.domain.medication.dto.TodayMedicationScheduleResponse
import com.safemedi.app.sefemedi.domain.medication.service.TodayMedicationScheduleService
import com.safemedi.app.sefemedi.global.error.BusinessException
import com.safemedi.app.sefemedi.global.error.ErrorCode
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/medication-records")
class MedicationRecordController(
    private val todayMedicationScheduleService: TodayMedicationScheduleService,
) {
    @GetMapping("/today")
    fun findTodaySchedules(
        @AuthenticationPrincipal socialId: String?,
    ): TodayMedicationScheduleResponse {
        return todayMedicationScheduleService.findTodaySchedules(
            socialId = socialId ?: throw BusinessException(ErrorCode.INVALID_TOKEN),
        )
    }
}
