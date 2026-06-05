package com.safemedi.app.sefemedi.domain.family.repository

import com.safemedi.app.sefemedi.domain.family.entity.Family
import org.springframework.data.jpa.repository.JpaRepository

interface FamilyRepository : JpaRepository<Family, Long> {

    fun findAllByUser_IdOrderByCreatedAtAsc(
        userId: Long,
    ): List<Family>
}
