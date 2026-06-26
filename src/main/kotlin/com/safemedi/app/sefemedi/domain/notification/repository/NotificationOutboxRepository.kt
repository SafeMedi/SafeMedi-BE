package com.safemedi.app.sefemedi.domain.notification.repository

import com.safemedi.app.sefemedi.domain.notification.entity.NotificationOutbox
import org.springframework.data.jpa.repository.JpaRepository

interface NotificationOutboxRepository : JpaRepository<NotificationOutbox, Long>
