package com.safemedi.app.sefemedi.domain.user.entity

import com.safemedi.app.sefemedi.global.entity.BaseTimeEntity
import jakarta.persistence.*
import java.time.LocalDate

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
    var isTutorialCompleted: Boolean = false,

    @Column(name = "birth_date")
    var birthDate: LocalDate? = null,

    @Enumerated(EnumType.STRING)
    var gender: Gender? = null,

    var height: Int? = null,
    var weight: Int? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "blood_type")
    var bloodType: BloodType? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "rh_type")
    var rhType: RhType = RhType.PLUS,

    @Column(columnDefinition = "text")
    var diseases: String? = null,

    @Column(name = "device_token")
    var deviceToken: String? = null,

    @Column(name = "device_type", length = 20)
    var deviceType: String? = null,

    @Column(name = "is_my_reminder_on")
    var isMyReminderOn: Boolean = true,

    @Column(name = "is_family_reminder_on")
    var isFamilyReminderOn: Boolean = true,

    @Column(name = "is_missed_alert_on")
    var isMissedAlertOn: Boolean = true
) : BaseTimeEntity()
