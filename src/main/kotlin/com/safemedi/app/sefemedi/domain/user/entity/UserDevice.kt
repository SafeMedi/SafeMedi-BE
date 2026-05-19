package com.safemedi.app.sefemedi.domain.user.entity

import com.safemedi.app.sefemedi.global.entity.BaseTimeEntity
import jakarta.persistence.*

@Entity
@Table(name = "user_device")
class UserDevice(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    val user: User,

    @Column(name = "device_token")
    var deviceToken: String,

    @Column(name = "device_type", length = 20)
    var deviceType: String,

    @Column(name = "is_my_reminder_on")
    var isMyReminderOn: Boolean = true,

    @Column(name = "is_family_reminder_on")
    var isFamilyReminderOn: Boolean = true,

    @Column(name = "is_missed_alert_on")
    var isMissedAlertOn: Boolean = true
) : BaseTimeEntity()
