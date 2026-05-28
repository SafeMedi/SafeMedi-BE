package com.safemedi.app.sefemedi.domain.drug.repository

import com.safemedi.app.sefemedi.domain.drug.entity.DiseaseMaster
import org.springframework.data.jpa.repository.JpaRepository

interface DiseaseMasterRepository : JpaRepository<DiseaseMaster, String>
