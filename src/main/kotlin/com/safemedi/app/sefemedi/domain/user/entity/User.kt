package com.safemedi.app.sefemedi.domain.user.entity

import com.safemedi.app.sefemedi.global.entity.BaseTimeEntity
import jakarta.persistence.*

@Entity
@Table(name = "user")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(length = 50)
    var nickname: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "social_provider")
    val socialProvider: SocialProvider? = null,

    @Column(name = "social_id", unique = true)
    val socialId: String? = null,

    @Column(name = "invite_code", length = 20)
    var inviteCode: String? = null,

    @Column(name = "is_tutorial_completed")
    var isTutorialCompleted: Boolean = false
) : BaseTimeEntity()
