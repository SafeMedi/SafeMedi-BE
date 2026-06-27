package com.safemedi.app.sefemedi.domain.notification.service

import com.safemedi.app.sefemedi.domain.notification.entity.NotificationLog
import com.safemedi.app.sefemedi.domain.notification.entity.NotificationType
import com.safemedi.app.sefemedi.domain.notification.repository.NotificationLogRepository
import com.safemedi.app.sefemedi.domain.user.entity.User
import com.safemedi.app.sefemedi.domain.user.repository.UserRepository
import com.safemedi.app.sefemedi.global.error.BusinessException
import com.safemedi.app.sefemedi.global.error.ErrorCode
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.mock
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class NotificationReadServiceTest {

    private lateinit var userRepository: UserRepository
    private lateinit var notificationLogRepository: NotificationLogRepository
    private lateinit var notificationReadService: NotificationReadService

    private val user = User(
        id = 1L,
        socialId = "kakao-123",
    )

    @BeforeEach
    fun setUp() {
        userRepository = mock(UserRepository::class.java)
        notificationLogRepository = mock(NotificationLogRepository::class.java)
        notificationReadService = NotificationReadService(
            userRepository = userRepository,
            notificationLogRepository = notificationLogRepository,
        )
    }

    @Test
    fun `markAsRead reads notification`() {
        val notification = notificationLog(
            id = 105L,
            user = user,
            isRead = false,
        )

        given(userRepository.findBySocialId("kakao-123")).willReturn(user)
        given(notificationLogRepository.findById(105L)).willReturn(Optional.of(notification))

        val response = notificationReadService.markAsRead(
            socialId = "kakao-123",
            notificationId = 105L,
        )

        assertEquals(105L, response.notificationId)
        assertEquals(true, response.isRead)
        assertEquals(true, notification.isRead)
    }

    @Test
    fun `markAsRead succeeds when notification is already read`() {
        val notification = notificationLog(
            id = 105L,
            user = user,
            isRead = true,
        )

        given(userRepository.findBySocialId("kakao-123")).willReturn(user)
        given(notificationLogRepository.findById(105L)).willReturn(Optional.of(notification))

        val response = notificationReadService.markAsRead(
            socialId = "kakao-123",
            notificationId = 105L,
        )

        assertEquals(105L, response.notificationId)
        assertEquals(true, response.isRead)
    }

    @Test
    fun `markAsRead throws when notification not found`() {
        given(userRepository.findBySocialId("kakao-123")).willReturn(user)
        given(notificationLogRepository.findById(999L)).willReturn(Optional.empty())

        val exception = assertFailsWith<BusinessException> {
            notificationReadService.markAsRead(
                socialId = "kakao-123",
                notificationId = 999L,
            )
        }

        assertEquals(ErrorCode.NOTIFICATION_NOT_FOUND, exception.errorCode)
    }

    @Test
    fun `markAsRead throws when notification belongs to another user`() {
        val otherUser = User(
            id = 2L,
            socialId = "kakao-456",
        )
        val notification = notificationLog(
            id = 105L,
            user = otherUser,
            isRead = false,
        )

        given(userRepository.findBySocialId("kakao-123")).willReturn(user)
        given(notificationLogRepository.findById(105L)).willReturn(Optional.of(notification))

        val exception = assertFailsWith<BusinessException> {
            notificationReadService.markAsRead(
                socialId = "kakao-123",
                notificationId = 105L,
            )
        }

        assertEquals(ErrorCode.NOTIFICATION_ACCESS_DENIED, exception.errorCode)
        assertEquals(false, notification.isRead)
    }

    private fun notificationLog(
        id: Long,
        user: User,
        isRead: Boolean,
    ): NotificationLog {
        return NotificationLog(
            id = id,
            user = user,
            type = NotificationType.MEDICATION_REMINDER,
            title = "Medication reminder",
            message = "Time to take medicine",
            isRead = isRead,
        )
    }
}
