package com.safemedi.app.sefemedi.domain.family.repository

import com.safemedi.app.sefemedi.domain.family.entity.Family
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.EntityGraph

interface FamilyRepository : JpaRepository<Family, Long> {

    @EntityGraph(attributePaths = ["connectedUser"], type = EntityGraph.EntityGraphType.FETCH)
    fun findAllByUser_IdOrderByCreatedAtAsc(
        userId: Long,
    ): List<Family>

    @EntityGraph(attributePaths = ["connectedUser"], type = EntityGraph.EntityGraphType.FETCH)
    fun findByIdAndUser_Id(
        id: Long,
        userId: Long,
    ): Family?
}
