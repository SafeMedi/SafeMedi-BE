package com.safemedi.app.sefemedi.domain.family.repository

import com.safemedi.app.sefemedi.domain.family.entity.Family
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface FamilyRepository : JpaRepository<Family, Long> {

    @EntityGraph(attributePaths = ["connectedUser"], type = EntityGraph.EntityGraphType.FETCH)
    fun findAllByUser_IdOrderByCreatedAtAsc(
        userId: Long,
    ): List<Family>

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
        """
        delete from Family f
        where f.user.id = :userId
           or f.connectedUser.id = :userId
        """
    )
    fun deleteAllByUser_IdOrConnectedUser_Id(
        @Param("userId") userId: Long,
    ): Int
}
