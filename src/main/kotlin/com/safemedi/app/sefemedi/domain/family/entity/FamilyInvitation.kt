package com.safemedi.app.sefemedi.domain.family.entity

import com.safemedi.app.sefemedi.domain.user.entity.User
import com.safemedi.app.sefemedi.global.entity.BaseTimeEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "family_invitation")
class FamilyInvitation(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inviter_id", nullable = false)
    val inviter: User,

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    val tokenHash: String,

    @Column(name = "encrypted_token", nullable = false, length = 255)
    val encryptedToken: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    val status: FamilyInvitationStatus = FamilyInvitationStatus.PENDING,

    @Column(name = "expires_at", nullable = false)
    val expiresAt: LocalDateTime,
) : BaseTimeEntity()
