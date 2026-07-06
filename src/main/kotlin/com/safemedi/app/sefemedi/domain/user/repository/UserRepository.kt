package com.safemedi.app.sefemedi.domain.user.repository

import com.safemedi.app.sefemedi.domain.user.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface UserRepository : JpaRepository<User, Long> {

    @Query(
        """
        select u
        from User u
        where u.socialId = :socialId
          and u.deletedAt is null
        """
    )
    fun findBySocialId(
        @Param("socialId") socialId: String,
    ): User?

    @Query(
        """
        select u
        from User u
        where u.socialId = :socialId
        """
    )
    fun findBySocialIdIncludingDeleted(
        @Param("socialId") socialId: String,
    ): User?
}
