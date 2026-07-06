package com.safemedi.app.sefemedi.domain.user.repository

import com.safemedi.app.sefemedi.domain.user.entity.UserDevice
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface UserDeviceRepository : JpaRepository<UserDevice, Long> {

    fun findFirstByUser_IdOrderByCreatedAtDesc(
        userId: Long,
    ): UserDevice?

    fun findByDeviceToken(
        deviceToken: String,
    ): UserDevice?

    fun findFirstByUser_IdAndIsActiveTrueOrderByCreatedAtDesc(
        userId: Long,
    ): UserDevice?

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
        """
        delete from UserDevice ud
        where ud.user.id = :userId
        """
    )
    fun deleteAllByUser_Id(
        @Param("userId") userId: Long,
    ): Int
}
