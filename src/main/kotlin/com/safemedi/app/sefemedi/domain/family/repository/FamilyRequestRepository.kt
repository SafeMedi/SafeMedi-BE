package com.safemedi.app.sefemedi.domain.family.repository

import com.safemedi.app.sefemedi.domain.family.entity.FamilyRequest
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface FamilyRequestRepository : JpaRepository<FamilyRequest, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
        """
        delete from FamilyRequest fr
        where fr.sender.id = :userId
           or fr.receiver.id = :userId
        """
    )
    fun deleteAllBySenderIdOrReceiverId(
        @Param("userId") userId: Long,
    ): Int
}
