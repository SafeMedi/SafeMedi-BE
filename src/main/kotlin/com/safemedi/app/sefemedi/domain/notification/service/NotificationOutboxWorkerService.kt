package com.safemedi.app.sefemedi.domain.notification.service

import com.safemedi.app.sefemedi.domain.notification.entity.NotificationOutbox
import com.safemedi.app.sefemedi.domain.notification.entity.NotificationOutboxStatus
import com.safemedi.app.sefemedi.domain.notification.fcm.NotificationPushRequest
import com.safemedi.app.sefemedi.domain.notification.fcm.NotificationPushSender
import com.safemedi.app.sefemedi.domain.notification.fcm.NotificationPushStatus
import com.safemedi.app.sefemedi.domain.notification.repository.NotificationOutboxRepository
import com.safemedi.app.sefemedi.domain.user.entity.UserDevice
import com.safemedi.app.sefemedi.domain.user.repository.UserDeviceRepository
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.data.domain.PageRequest
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@ConditionalOnProperty(prefix = "firebase", name = ["enabled"], havingValue = "true")
class NotificationOutboxWorkerService(
    private val notificationOutboxRepository: NotificationOutboxRepository,
    private val userDeviceRepository: UserDeviceRepository,
    private val notificationPushSender: NotificationPushSender,
) {

    @Transactional
    fun processDueOutboxes(
        now: LocalDateTime,
        batchSize: Int,
    ): Int {
        val outboxes = notificationOutboxRepository.findDueOutboxes(
            pendingStatus = NotificationOutboxStatus.PENDING,
            failedStatus = NotificationOutboxStatus.FAILED,
            now = now,
            pageable = PageRequest.of(0, batchSize),
        )

        outboxes.forEach { processOutbox(it, now) }

        return outboxes.size
    }

    private fun processOutbox(
        outbox: NotificationOutbox,
        now: LocalDateTime,
    ) {
        outbox.markProcessing()

        val userId = outbox.user.id
        if (userId == null) {
            log.warn(
                "FCM 발송 실패: 사용자 ID가 없어 알림 작업을 DEAD 처리합니다. outboxId={}, eventKey={}",
                outbox.id,
                outbox.eventKey,
            )
            outbox.markDead()
            return
        }

        val userDevice = userDeviceRepository.findFirstByUser_IdAndIsActiveTrueOrderByCreatedAtDesc(userId)
        if (userDevice == null) {
            log.warn(
                "FCM 발송 실패: 활성 기기 토큰이 없어 알림 작업을 DEAD 처리합니다. outboxId={}, userId={}, eventKey={}",
                outbox.id,
                userId,
                outbox.eventKey,
            )
            outbox.markDead()
            return
        }

        val result = notificationPushSender.send(
            NotificationPushRequest(
                token = userDevice.deviceToken,
                title = outbox.notificationLog.title,
                body = outbox.notificationLog.message,
                data = createData(outbox),
            )
        )

        when (result.status) {
            NotificationPushStatus.SUCCESS -> {
                log.info(
                    "FCM 발송 성공. outboxId={}, userId={}, notificationId={}",
                    outbox.id,
                    userId,
                    outbox.notificationLog.id,
                )
                outbox.markSent()
            }
            NotificationPushStatus.INVALID_TOKEN -> {
                log.warn(
                    "FCM 발송 실패: 유효하지 않은 기기 토큰입니다. outboxId={}, userId={}, deviceId={}, errorCode={}",
                    outbox.id,
                    userId,
                    userDevice.id,
                    result.errorCode,
                )
                userDevice.deactivate()
                outbox.markDead()
            }
            NotificationPushStatus.TEMPORARY_FAILURE -> {
                log.warn(
                    "FCM 발송 일시 실패: 재시도 대상으로 처리합니다. outboxId={}, userId={}, retryCount={}, errorCode={}",
                    outbox.id,
                    userId,
                    outbox.retryCount,
                    result.errorCode,
                )
                scheduleRetryOrDead(outbox, now)
            }
            NotificationPushStatus.PERMANENT_FAILURE -> {
                log.warn(
                    "FCM 발송 영구 실패: 알림 작업을 DEAD 처리합니다. outboxId={}, userId={}, errorCode={}",
                    outbox.id,
                    userId,
                    result.errorCode,
                )
                outbox.markDead()
            }
        }
    }

    private fun createData(
        outbox: NotificationOutbox,
    ): Map<String, String> {
        val notificationLog = outbox.notificationLog
        val data = mutableMapOf(
            "notificationId" to requireNotNull(notificationLog.id).toString(),
            "type" to notificationLog.type.name,
        )
        notificationLog.targetType?.let { data["targetType"] = it.name }
        notificationLog.targetId?.let { data["targetId"] = it.toString() }

        return data
    }

    private fun scheduleRetryOrDead(
        outbox: NotificationOutbox,
        now: LocalDateTime,
    ) {
        if (outbox.retryCount >= MAX_RETRY_COUNT) {
            outbox.markDead()
            return
        }

        outbox.markRetryScheduled(
            nextRetryAt = now.plusMinutes(retryDelayMinutes(outbox.retryCount)),
        )
    }

    private fun retryDelayMinutes(
        retryCount: Int,
    ): Long {
        return RETRY_DELAYS_MINUTES.getOrElse(retryCount) { RETRY_DELAYS_MINUTES.last() }
    }

    private companion object {
        private val log = LoggerFactory.getLogger(NotificationOutboxWorkerService::class.java)
        const val MAX_RETRY_COUNT = 3
        val RETRY_DELAYS_MINUTES = listOf(1L, 5L, 30L)
    }
}
