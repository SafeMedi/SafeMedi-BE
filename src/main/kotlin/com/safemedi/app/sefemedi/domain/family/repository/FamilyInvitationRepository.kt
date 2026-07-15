package com.safemedi.app.sefemedi.domain.family.repository

import com.safemedi.app.sefemedi.domain.family.entity.FamilyInvitation
import com.safemedi.app.sefemedi.domain.family.entity.FamilyInvitationStatus
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime

interface FamilyInvitationRepository : JpaRepository<FamilyInvitation, Long> {
    fun findByTokenHash(tokenHash: String): FamilyInvitation?

    fun findFirstByInviter_IdAndStatusAndExpiresAtAfterOrderByCreatedAtDesc(
        inviterId: Long,
        status: FamilyInvitationStatus,
        now: LocalDateTime,
    ): FamilyInvitation?
}
