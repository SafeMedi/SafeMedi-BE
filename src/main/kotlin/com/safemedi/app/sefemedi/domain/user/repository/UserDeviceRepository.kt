package com.safemedi.app.sefemedi.domain.user.repository

import com.safemedi.app.sefemedi.domain.user.entity.UserDevice
import org.springframework.data.jpa.repository.JpaRepository

interface UserDeviceRepository : JpaRepository<UserDevice, Long> {

    fun findFirstByUser_IdOrderByCreatedAtDesc(
        userId: Long,
    ): UserDevice?
}
