package com.safemedi.app.sefemedi.domain.medication.controller

import com.safemedi.app.sefemedi.domain.medication.dto.PrescriptionCreateRequest
import com.safemedi.app.sefemedi.domain.medication.dto.PrescriptionCreateResponse
import com.safemedi.app.sefemedi.domain.medication.dto.PrescriptionListResponse
import com.safemedi.app.sefemedi.domain.medication.service.PrescriptionCreateService
import com.safemedi.app.sefemedi.domain.medication.service.PrescriptionQueryService
import com.safemedi.app.sefemedi.global.error.BusinessException
import com.safemedi.app.sefemedi.global.error.ErrorCode
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/prescriptions")
class PrescriptionController(
    private val prescriptionCreateService: PrescriptionCreateService,
    private val prescriptionQueryService: PrescriptionQueryService,
) {
    @GetMapping
    fun findPrescriptions(
        @AuthenticationPrincipal socialId: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
    ): PrescriptionListResponse {
        return prescriptionQueryService.findPrescriptions(
            socialId = socialId ?: throw BusinessException(ErrorCode.INVALID_TOKEN),
            page = page,
            size = size,
        )
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @AuthenticationPrincipal socialId: String?,
        @RequestBody request: PrescriptionCreateRequest,
    ): PrescriptionCreateResponse {
        return prescriptionCreateService.create(
            socialId = socialId ?: throw BusinessException(ErrorCode.INVALID_TOKEN),
            request = request,
        )
    }
}
