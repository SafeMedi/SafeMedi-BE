package com.safemedi.app.sefemedi.domain.drug.repository

import com.safemedi.app.sefemedi.domain.drug.entity.AtcGroupMaster
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.jpa.repository.JpaRepository

interface AtcGroupMasterRepository : JpaRepository<AtcGroupMaster, String> {
    fun findByAtcNameKoContainingOrAtcNameEnContainingOrderByAtcNameKoAsc(
        atcNameKo: String,
        atcNameEn: String,
        pageable: Pageable,
    ): Slice<AtcGroupMaster>
}
