package com.safemedi.app.sefemedi.domain.drug.service

import com.safemedi.app.sefemedi.domain.drug.dto.DrugSearchResponse
import com.safemedi.app.sefemedi.domain.drug.repository.DrugMasterRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DrugSearchService(
    private val drugMasterRepository: DrugMasterRepository,
) {
    @Transactional(readOnly = true)
    fun search(keyword: String): List<DrugSearchResponse> =
        drugMasterRepository.findByDrugNameContainingAndAtcCodeIsNotNullOrderByDrugNameAsc(keyword)
            .mapNotNull {
                DrugSearchResponse(
                    drugCode = it.drugCode,
                    atcCode = it.atcCode ?: return@mapNotNull null,
                    drugName = it.drugName ?: return@mapNotNull null,
                )
            }
}
