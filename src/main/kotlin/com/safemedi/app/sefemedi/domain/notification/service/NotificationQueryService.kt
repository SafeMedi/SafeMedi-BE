package com.safemedi.app.sefemedi.domain.notification.service

import com.safemedi.app.sefemedi.domain.notification.dto.NotificationListResponse
import com.safemedi.app.sefemedi.domain.notification.dto.NotificationSummaryResponse
import com.safemedi.app.sefemedi.domain.notification.repository.NotificationLogRepository
import com.safemedi.app.sefemedi.domain.user.repository.UserRepository
import com.safemedi.app.sefemedi.global.error.BusinessException
import com.safemedi.app.sefemedi.global.error.ErrorCode
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class NotificationQueryService(
    private val userRepository: UserRepository,
    private val notificationLogRepository: NotificationLogRepository,
) {

    @Transactional(readOnly = true)
    fun findNotifications(
        socialId: String,
        page: Int,
        size: Int,
    ): NotificationListResponse {
        validatePageRequest(page, size)

        val user = userRepository.findBySocialId(socialId)
            ?: throw BusinessException(ErrorCode.INVALID_TOKEN)
        val userId = user.id ?: throw BusinessException(ErrorCode.INVALID_TOKEN)
        val notifications = notificationLogRepository.findByUser_IdOrderByCreatedAtDescIdDesc(
            userId = userId,
            pageable = PageRequest.of(page, size),
        )

        return NotificationListResponse(
            content = notifications.content.map {
                NotificationSummaryResponse(
                    notificationId = it.id ?: throw BusinessException(ErrorCode.INTERNAL_SERVER_ERROR),
                    type = it.type,
                    title = it.title,
                    message = it.message,
                    isRead = it.isRead,
                    targetType = it.targetType,
                    targetId = it.targetId,
                    createdAt = it.createdAt ?: throw BusinessException(ErrorCode.INTERNAL_SERVER_ERROR),
                )
            },
            page = page,
            size = size,
            isLast = notifications.isLast,
        )
    }

    private fun validatePageRequest(
        page: Int,
        size: Int,
    ) {
        if (page < 0 || size < 1 || size > MAX_PAGE_SIZE) {
            throw BusinessException(ErrorCode.INVALID_PAGE_REQUEST)
        }
    }

    companion object {
        private const val MAX_PAGE_SIZE = 100
    }
}
