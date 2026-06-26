package com.safemedi.app.sefemedi.domain.notification.repository

import com.safemedi.app.sefemedi.domain.notification.entity.NotificationLog
import org.springframework.data.jpa.repository.JpaRepository

interface NotificationLogRepository : JpaRepository<NotificationLog, Long> {

    fun findByDeduplicationKey(
        deduplicationKey: String,
    ): NotificationLog?
}
