package com.safemedi.app.sefemedi.domain.user.entity

import jakarta.persistence.*

@Entity
@Table(name = "users")
class User(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val kakaoId: Long,

    val isTutorialCompleted: Boolean = false
)