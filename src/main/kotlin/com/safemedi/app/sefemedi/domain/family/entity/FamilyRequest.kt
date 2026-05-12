package com.safemedi.app.sefemedi.domain.family.entity

import com.safemedi.app.sefemedi.domain.user.entity.User
import com.safemedi.app.sefemedi.global.entity.BaseTimeEntity
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import java.time.LocalDateTime

@Entity
@Table(name = "family_request")
class FamilyRequest(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    val sender: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id")
    val receiver: User,

    @Column(name = "proposed_relation", length = 50)
    var proposedRelation: String,

    @Enumerated(EnumType.STRING)
    var status: FamilyRequestStatus = FamilyRequestStatus.PENDING
) : BaseTimeEntity()
