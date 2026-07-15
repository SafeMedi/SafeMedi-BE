package com.safemedi.app.sefemedi.domain.family.service

import com.safemedi.app.sefemedi.domain.family.dto.FamilyInvitationAcceptResponse
import com.safemedi.app.sefemedi.domain.family.entity.Family
import com.safemedi.app.sefemedi.domain.family.entity.FamilyInvitationStatus
import com.safemedi.app.sefemedi.domain.family.repository.FamilyInvitationRepository
import com.safemedi.app.sefemedi.domain.family.repository.FamilyRepository
import com.safemedi.app.sefemedi.domain.user.repository.UserRepository
import com.safemedi.app.sefemedi.global.error.BusinessException
import com.safemedi.app.sefemedi.global.error.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneOffset

@Service
class FamilyInvitationAcceptService(
    private val userRepository: UserRepository,
    private val familyInvitationRepository: FamilyInvitationRepository,
    private val familyRepository: FamilyRepository,
    private val tokenHasher: FamilyInvitationTokenHasher,
    private val clock: Clock,
) {

    @Transactional
    fun accept(
        socialId: String,
        token: String,
    ): FamilyInvitationAcceptResponse {
        val acceptingUser = userRepository.findBySocialId(socialId)
            ?: throw BusinessException(ErrorCode.INVALID_ACCESS_TOKEN)
        val invitation = familyInvitationRepository.findByTokenHashForUpdate(tokenHasher.hash(token))
            ?: throw BusinessException(ErrorCode.FAMILY_INVITATION_NOT_FOUND)
        val acceptedInstant = clock.instant()
        val acceptedAt = LocalDateTime.ofInstant(acceptedInstant, ZoneOffset.UTC)

        if (!invitation.expiresAt.isAfter(acceptedAt)) {
            throw BusinessException(ErrorCode.FAMILY_INVITATION_EXPIRED)
        }
        if (invitation.status != FamilyInvitationStatus.PENDING) {
            throw BusinessException(ErrorCode.FAMILY_INVITATION_ALREADY_USED)
        }

        val inviter = invitation.inviter
        if (inviter.id == acceptingUser.id) {
            throw BusinessException(ErrorCode.FAMILY_INVITATION_SELF_ACCESS)
        }
        val inviterName = inviter.nickname
            ?: throw BusinessException(ErrorCode.FAMILY_INVITATION_INVITER_NAME_NOT_FOUND)
        val acceptingUserId = acceptingUser.id
            ?: throw BusinessException(ErrorCode.INTERNAL_SERVER_ERROR)
        val inviterId = inviter.id
            ?: throw BusinessException(ErrorCode.INTERNAL_SERVER_ERROR)

        if (isAlreadyConnected(acceptingUserId, inviterId)) {
            throw BusinessException(ErrorCode.FAMILY_INVITATION_ALREADY_CONNECTED)
        }

        invitation.accept(
            acceptedBy = acceptingUser,
            acceptedAt = acceptedAt,
        )
        val acceptingUserFamily = familyRepository.save(
            Family(
                user = acceptingUser,
                connectedUser = inviter,
                relation = DEFAULT_RELATION,
            )
        )
        familyRepository.save(
            Family(
                user = inviter,
                connectedUser = acceptingUser,
                relation = DEFAULT_RELATION,
            )
        )

        val familyId = acceptingUserFamily.id
            ?: throw BusinessException(ErrorCode.INTERNAL_SERVER_ERROR)

        return FamilyInvitationAcceptResponse(
            familyId = familyId,
            name = inviterName,
            relation = DEFAULT_RELATION,
            connectedAt = acceptedInstant,
        )
    }

    private fun isAlreadyConnected(
        acceptingUserId: Long,
        inviterId: Long,
    ): Boolean {
        return familyRepository.countConnectionsBetween(acceptingUserId, inviterId) > 0
    }

    private companion object {
        const val DEFAULT_RELATION = "가족"
    }
}
