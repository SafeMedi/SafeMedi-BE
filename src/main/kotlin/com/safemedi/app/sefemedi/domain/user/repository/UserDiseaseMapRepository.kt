package com.safemedi.app.sefemedi.domain.user.repository

import com.safemedi.app.sefemedi.domain.user.entity.UserDiseaseMap
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.EntityGraph

interface UserDiseaseMapRepository : JpaRepository<UserDiseaseMap, Long> {

    @EntityGraph(attributePaths = ["disease"], type = EntityGraph.EntityGraphType.FETCH)
    fun findAllByUser_IdOrderByCreatedAtAsc(
        userId: Long,
    ): List<UserDiseaseMap>
}
