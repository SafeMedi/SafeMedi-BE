package com.safemedi.app.sefemedi.domain.user.repository

import com.safemedi.app.sefemedi.domain.user.entity.UserDiseaseMap
import org.springframework.data.jpa.repository.JpaRepository

interface UserDiseaseMapRepository : JpaRepository<UserDiseaseMap, Long> {

    fun findAllByUser_IdOrderByCreatedAtAsc(
        userId: Long,
    ): List<UserDiseaseMap>
}
