package com.safemedi.app.sefemedi.domain.notification.repository

import com.safemedi.app.sefemedi.domain.notification.entity.NotificationLog
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.jpa.repository.JpaRepository

interface NotificationLogRepository : JpaRepository<NotificationLog, Long> {

    fun findByDeduplicationKey(
        deduplicationKey: String,
    ): NotificationLog?

    fun findByUserId(
        userId: Long,
        pageable: Pageable,
    ): Slice<NotificationLog>

    fun countByUserIdAndIsReadFalse(
        userId: Long,
    ): Long
}
