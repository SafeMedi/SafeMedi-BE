package com.safemedi.app.sefemedi.domain.family.service

import com.safemedi.app.sefemedi.domain.family.entity.Family
import com.safemedi.app.sefemedi.domain.family.entity.FamilyInvitation
import com.safemedi.app.sefemedi.domain.family.entity.FamilyInvitationStatus
import com.safemedi.app.sefemedi.domain.family.repository.FamilyInvitationRepository
import com.safemedi.app.sefemedi.domain.family.repository.FamilyRepository
import com.safemedi.app.sefemedi.domain.user.entity.User
import com.safemedi.app.sefemedi.domain.user.repository.UserRepository
import com.safemedi.app.sefemedi.global.error.BusinessException
import com.safemedi.app.sefemedi.global.error.ErrorCode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.BDDMockito.given
import org.mockito.Mockito.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

class FamilyInvitationAcceptServiceTest {
    private lateinit var userRepository: UserRepository
    private lateinit var familyInvitationRepository: FamilyInvitationRepository
    private lateinit var familyRepository: FamilyRepository
    private lateinit var tokenHasher: FamilyInvitationTokenHasher
    private lateinit var service: FamilyInvitationAcceptService

    private val acceptingUser = User(id = 1L, nickname = "수락자", socialId = "kakao-123")
    private val inviter = User(id = 2L, nickname = "홍길동", socialId = "kakao-456")
    private val now = LocalDateTime.of(2026, 7, 15, 6, 5)

    @BeforeEach
    fun setUp() {
        userRepository = mock(UserRepository::class.java)
        familyInvitationRepository = mock(FamilyInvitationRepository::class.java)
        familyRepository = mock(FamilyRepository::class.java)
        tokenHasher = mock(FamilyInvitationTokenHasher::class.java)
        service = FamilyInvitationAcceptService(
            userRepository = userRepository,
            familyInvitationRepository = familyInvitationRepository,
            familyRepository = familyRepository,
            tokenHasher = tokenHasher,
            clock = Clock.fixed(Instant.parse("2026-07-15T06:05:00Z"), ZoneOffset.UTC),
        )

        given(userRepository.findBySocialId("kakao-123")).willReturn(acceptingUser)
        given(tokenHasher.hash("raw-token")).willReturn("token-hash")
    }

    @Test
    fun `초대를 수락하면 양방향 가족 관계와 수락 정보를 저장한다`() {
        val invitation = invitation()
        val acceptingFamilyCaptor = ArgumentCaptor.forClass(Family::class.java)
        val inviterFamilyCaptor = ArgumentCaptor.forClass(Family::class.java)

        given(familyInvitationRepository.findByTokenHashForUpdate("token-hash")).willReturn(invitation)
        given(familyRepository.existsByUser_IdAndConnectedUser_Id(1L, 2L)).willReturn(false)
        given(familyRepository.existsByUser_IdAndConnectedUser_Id(2L, 1L)).willReturn(false)
        given(familyRepository.saveAndFlush(acceptingFamilyCaptor.capture())).willAnswer {
            Family(
                id = 12L,
                user = acceptingFamilyCaptor.value.user,
                connectedUser = acceptingFamilyCaptor.value.connectedUser,
                relation = acceptingFamilyCaptor.value.relation,
            )
        }
        given(familyRepository.save(inviterFamilyCaptor.capture())).willAnswer { inviterFamilyCaptor.value }

        val response = service.accept("kakao-123", "raw-token")

        assertEquals(12L, response.familyId)
        assertEquals("홍길동", response.name)
        assertEquals("가족", response.relation)
        assertEquals(Instant.parse("2026-07-15T06:05:00Z"), response.connectedAt)
        assertEquals(acceptingUser, acceptingFamilyCaptor.value.user)
        assertEquals(inviter, acceptingFamilyCaptor.value.connectedUser)
        assertEquals("가족", acceptingFamilyCaptor.value.relation)
        assertEquals(inviter, inviterFamilyCaptor.value.user)
        assertEquals(acceptingUser, inviterFamilyCaptor.value.connectedUser)
        assertEquals("가족", inviterFamilyCaptor.value.relation)
        assertEquals(FamilyInvitationStatus.ACCEPTED, invitation.status)
        assertEquals(acceptingUser, invitation.acceptedBy)
        assertEquals(now, invitation.acceptedAt)
    }

    @Test
    fun `현재 사용자가 없으면 AUTH_003 예외를 던진다`() {
        given(userRepository.findBySocialId("missing-user")).willReturn(null)

        assertError(ErrorCode.INVALID_ACCESS_TOKEN) {
            service.accept("missing-user", "raw-token")
        }

        verifyNoInteractions(familyInvitationRepository, familyRepository)
    }

    @Test
    fun `초대가 없으면 INV_001 예외를 던진다`() {
        given(familyInvitationRepository.findByTokenHashForUpdate("token-hash")).willReturn(null)

        assertError(ErrorCode.FAMILY_INVITATION_NOT_FOUND) {
            service.accept("kakao-123", "raw-token")
        }
    }

    @Test
    fun `초대가 만료되었으면 INV_002 예외를 던진다`() {
        given(familyInvitationRepository.findByTokenHashForUpdate("token-hash")).willReturn(
            invitation(expiresAt = now)
        )

        assertError(ErrorCode.FAMILY_INVITATION_EXPIRED) {
            service.accept("kakao-123", "raw-token")
        }
    }

    @Test
    fun `이미 사용된 초대이면 INV_003 예외를 던진다`() {
        given(familyInvitationRepository.findByTokenHashForUpdate("token-hash")).willReturn(
            invitation(status = FamilyInvitationStatus.ACCEPTED)
        )

        assertError(ErrorCode.FAMILY_INVITATION_ALREADY_USED) {
            service.accept("kakao-123", "raw-token")
        }
    }

    @Test
    fun `자신이 생성한 초대이면 INV_004 예외를 던진다`() {
        given(familyInvitationRepository.findByTokenHashForUpdate("token-hash")).willReturn(
            invitation(inviter = acceptingUser)
        )

        assertError(ErrorCode.FAMILY_INVITATION_SELF_ACCESS) {
            service.accept("kakao-123", "raw-token")
        }
    }

    @Test
    fun `초대자 이름이 없으면 INV_005 예외를 던진다`() {
        given(familyInvitationRepository.findByTokenHashForUpdate("token-hash")).willReturn(
            invitation(inviter = User(id = 2L, nickname = null, socialId = "kakao-456"))
        )

        assertError(ErrorCode.FAMILY_INVITATION_INVITER_NAME_NOT_FOUND) {
            service.accept("kakao-123", "raw-token")
        }
    }

    @Test
    fun `이미 가족 관계이면 INV_006 예외를 던진다`() {
        given(familyInvitationRepository.findByTokenHashForUpdate("token-hash")).willReturn(invitation())
        given(familyRepository.existsByUser_IdAndConnectedUser_Id(1L, 2L)).willReturn(true)

        assertError(ErrorCode.FAMILY_INVITATION_ALREADY_CONNECTED) {
            service.accept("kakao-123", "raw-token")
        }

        verify(familyRepository, never()).save(any(Family::class.java))
        verify(familyRepository, never()).saveAndFlush(any(Family::class.java))
    }

    private fun invitation(
        inviter: User = this.inviter,
        status: FamilyInvitationStatus = FamilyInvitationStatus.PENDING,
        expiresAt: LocalDateTime = now.plusHours(1),
    ): FamilyInvitation {
        return FamilyInvitation(
            id = 10L,
            inviter = inviter,
            tokenHash = "token-hash",
            encryptedToken = "encrypted-token",
            status = status,
            expiresAt = expiresAt,
        )
    }

    private fun assertError(
        errorCode: ErrorCode,
        block: () -> Unit,
    ) {
        val exception = assertThrows(BusinessException::class.java, block)
        assertEquals(errorCode, exception.errorCode)
    }
}
