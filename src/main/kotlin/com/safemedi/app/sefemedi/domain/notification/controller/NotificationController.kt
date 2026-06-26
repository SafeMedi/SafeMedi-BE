package com.safemedi.app.sefemedi.domain.notification.controller

import com.safemedi.app.sefemedi.domain.notification.dto.NotificationListResponse
import com.safemedi.app.sefemedi.domain.notification.service.NotificationQueryService
import com.safemedi.app.sefemedi.global.error.BusinessException
import com.safemedi.app.sefemedi.global.error.ErrorCode
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/notifications")
class NotificationController(
    private val notificationQueryService: NotificationQueryService,
) {

    @GetMapping
    fun findNotifications(
        @AuthenticationPrincipal socialId: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): NotificationListResponse {
        return notificationQueryService.findNotifications(
            socialId = socialId ?: throw BusinessException(ErrorCode.INVALID_TOKEN),
            page = page,
            size = size,
        )
    }
}
