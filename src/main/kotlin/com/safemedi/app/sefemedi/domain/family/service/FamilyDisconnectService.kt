package com.safemedi.app.sefemedi.domain.family.service

import com.safemedi.app.sefemedi.domain.family.repository.FamilyRepository
import com.safemedi.app.sefemedi.domain.user.repository.UserRepository
import com.safemedi.app.sefemedi.global.error.BusinessException
import com.safemedi.app.sefemedi.global.error.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FamilyDisconnectService(
    private val userRepository: UserRepository,
    private val familyRepository: FamilyRepository,
) {

    @Transactional
    fun disconnect(
        socialId: String,
        familyId: Long,
    ) {
        val currentUser = userRepository.findBySocialId(socialId)
            ?: throw BusinessException(ErrorCode.INVALID_ACCESS_TOKEN)
        val currentUserId = currentUser.id
            ?: throw BusinessException(ErrorCode.INTERNAL_SERVER_ERROR)
        val family = familyRepository.findWithConnectedUserById(familyId)
            ?: throw BusinessException(ErrorCode.FAMILY_NOT_FOUND)

        if (family.user.id != currentUserId) {
            throw BusinessException(ErrorCode.FAMILY_ACCESS_DENIED)
        }

        val connectedUserId = family.connectedUser.id
            ?: throw BusinessException(ErrorCode.INTERNAL_SERVER_ERROR)
        val firstUserId = minOf(currentUserId, connectedUserId)
        val secondUserId = maxOf(currentUserId, connectedUserId)
        val connections = familyRepository.findConnectionsBetweenForUpdate(
            firstUserId = firstUserId,
            secondUserId = secondUserId,
        )

        if (connections.none { it.id == familyId }) {
            throw BusinessException(ErrorCode.FAMILY_NOT_FOUND)
        }

        familyRepository.deleteAll(connections)
    }
}
