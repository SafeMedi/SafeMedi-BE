package com.safemedi.app.sefemedi.domain.notification.entity

import com.safemedi.app.sefemedi.domain.user.entity.User
import com.safemedi.app.sefemedi.global.entity.BaseTimeEntity
import jakarta.persistence.*

@Entity
@Table(name = "notification_log")
class NotificationLog(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    val user: User,

    @Column(length = 50)
    var type: String,

    @Column(length = 255)
    var title: String,

    @Column(columnDefinition = "text")
    var message: String,

    @Column(name = "is_read")
    var isRead: Boolean = false
) : BaseTimeEntity()
