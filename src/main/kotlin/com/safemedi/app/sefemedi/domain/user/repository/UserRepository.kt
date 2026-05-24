package com.safemedi.app.sefemedi.domain.user.repository

import com.safemedi.app.sefemedi.domain.user.entity.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Long> {

    fun findBySocialId(
        socialId: String
    ): User?
}
