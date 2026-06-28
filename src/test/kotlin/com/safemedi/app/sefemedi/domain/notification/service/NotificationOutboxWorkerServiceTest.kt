package com.safemedi.app.sefemedi.domain.notification.service

import com.safemedi.app.sefemedi.domain.notification.entity.NotificationLog
import com.safemedi.app.sefemedi.domain.notification.entity.NotificationOutbox
import com.safemedi.app.sefemedi.domain.notification.entity.NotificationOutboxStatus
import com.safemedi.app.sefemedi.domain.notification.entity.NotificationTargetType
import com.safemedi.app.sefemedi.domain.notification.entity.NotificationType
import com.safemedi.app.sefemedi.domain.notification.fcm.NotificationPushRequest
import com.safemedi.app.sefemedi.domain.notification.fcm.NotificationPushResult
import com.safemedi.app.sefemedi.domain.notification.fcm.NotificationPushSender
import com.safemedi.app.sefemedi.domain.notification.fcm.NotificationPushStatus
import com.safemedi.app.sefemedi.domain.notification.repository.NotificationOutboxRepository
import com.safemedi.app.sefemedi.domain.user.entity.User
import com.safemedi.app.sefemedi.domain.user.entity.UserDevice
import com.safemedi.app.sefemedi.domain.user.repository.UserDeviceRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.mock
import org.springframework.data.domain.PageRequest
import java.time.LocalDateTime
import kotlin.test.assertEquals

class NotificationOutboxWorkerServiceTest {

    private lateinit var notificationOutboxRepository: NotificationOutboxRepository
    private lateinit var userDeviceRepository: UserDeviceRepository
    private lateinit var notificationPushSender: FakeNotificationPushSender
    private lateinit var notificationOutboxWorkerService: NotificationOutboxWorkerService

    private val user = User(
        id = 1L,
        socialId = "kakao-123",
    )

    @BeforeEach
    fun setUp() {
        notificationOutboxRepository = mock(NotificationOutboxRepository::class.java)
        userDeviceRepository = mock(UserDeviceRepository::class.java)
        notificationPushSender = FakeNotificationPushSender()
        notificationOutboxWorkerService = NotificationOutboxWorkerService(
            notificationOutboxRepository = notificationOutboxRepository,
            userDeviceRepository = userDeviceRepository,
            notificationPushSender = notificationPushSender,
        )
    }

    @Test
    fun `처리 대상 아웃박스를 FCM으로 발송하고 SENT 상태로 변경한다`() {
        val now = LocalDateTime.of(2026, 6, 29, 9, 0)
        val outbox = notificationOutbox()
        val userDevice = userDevice()

        givenDueOutboxes(now, listOf(outbox))
        given(userDeviceRepository.findFirstByUser_IdAndIsActiveTrueOrderByCreatedAtDesc(1L)).willReturn(userDevice)
        notificationPushSender.result = NotificationPushResult(NotificationPushStatus.SUCCESS)

        val processedCount = notificationOutboxWorkerService.processDueOutboxes(now, batchSize = 50)

        val request = requireNotNull(notificationPushSender.requests.single())

        assertEquals(1, processedCount)
        assertEquals(NotificationOutboxStatus.SENT, outbox.status)
        assertEquals("device-token", request.token)
        assertEquals("Medication reminder", request.title)
        assertEquals("Time to take medicine", request.body)
        assertEquals("100", request.data["notificationId"])
        assertEquals("MEDICATION_REMINDER", request.data["type"])
        assertEquals("MEDICATION_RECORD", request.data["targetType"])
        assertEquals("500", request.data["targetId"])
    }

    @Test
    fun `유효하지 않은 토큰이면 기기 토큰을 비활성화하고 아웃박스를 DEAD 처리한다`() {
        val now = LocalDateTime.of(2026, 6, 29, 9, 0)
        val outbox = notificationOutbox()
        val userDevice = userDevice()

        givenDueOutboxes(now, listOf(outbox))
        given(userDeviceRepository.findFirstByUser_IdAndIsActiveTrueOrderByCreatedAtDesc(1L)).willReturn(userDevice)
        notificationPushSender.result = NotificationPushResult(NotificationPushStatus.INVALID_TOKEN, "UNREGISTERED")

        notificationOutboxWorkerService.processDueOutboxes(now, batchSize = 50)

        assertEquals(NotificationOutboxStatus.DEAD, outbox.status)
        assertEquals(false, userDevice.isActive)
    }

    @Test
    fun `일시 실패이면 재시도 시간을 예약하고 FAILED 상태로 변경한다`() {
        val now = LocalDateTime.of(2026, 6, 29, 9, 0)
        val outbox = notificationOutbox()
        val userDevice = userDevice()

        givenDueOutboxes(now, listOf(outbox))
        given(userDeviceRepository.findFirstByUser_IdAndIsActiveTrueOrderByCreatedAtDesc(1L)).willReturn(userDevice)
        notificationPushSender.result = NotificationPushResult(NotificationPushStatus.TEMPORARY_FAILURE, "UNAVAILABLE")

        notificationOutboxWorkerService.processDueOutboxes(now, batchSize = 50)

        assertEquals(NotificationOutboxStatus.FAILED, outbox.status)
        assertEquals(1, outbox.retryCount)
        assertEquals(now.plusMinutes(1), outbox.nextRetryAt)
    }

    @Test
    fun `활성 기기가 없으면 아웃박스를 DEAD 처리한다`() {
        val now = LocalDateTime.of(2026, 6, 29, 9, 0)
        val outbox = notificationOutbox()

        givenDueOutboxes(now, listOf(outbox))
        given(userDeviceRepository.findFirstByUser_IdAndIsActiveTrueOrderByCreatedAtDesc(1L)).willReturn(null)

        notificationOutboxWorkerService.processDueOutboxes(now, batchSize = 50)

        assertEquals(NotificationOutboxStatus.DEAD, outbox.status)
    }

    private fun givenDueOutboxes(
        now: LocalDateTime,
        outboxes: List<NotificationOutbox>,
    ) {
        given(
            notificationOutboxRepository.findDueOutboxes(
                pendingStatus = NotificationOutboxStatus.PENDING,
                failedStatus = NotificationOutboxStatus.FAILED,
                now = now,
                pageable = PageRequest.of(0, 50),
            )
        ).willReturn(outboxes)
    }

    private fun notificationOutbox(): NotificationOutbox {
        val notificationLog = NotificationLog(
            id = 100L,
            user = user,
            type = NotificationType.MEDICATION_REMINDER,
            title = "Medication reminder",
            message = "Time to take medicine",
            targetType = NotificationTargetType.MEDICATION_RECORD,
            targetId = 500L,
        )

        return NotificationOutbox(
            id = 10L,
            notificationLog = notificationLog,
            user = user,
            eventKey = "event-key",
            scheduledAt = LocalDateTime.of(2026, 6, 29, 9, 0),
        )
    }

    private fun userDevice(): UserDevice {
        return UserDevice(
            id = 20L,
            user = user,
            deviceToken = "device-token",
            deviceType = "ANDROID",
            isActive = true,
        )
    }

    private class FakeNotificationPushSender : NotificationPushSender {
        var result: NotificationPushResult = NotificationPushResult(NotificationPushStatus.SUCCESS)
        val requests: MutableList<NotificationPushRequest> = mutableListOf()

        override fun send(
            request: NotificationPushRequest,
        ): NotificationPushResult {
            requests.add(request)
            return result
        }
    }
}
