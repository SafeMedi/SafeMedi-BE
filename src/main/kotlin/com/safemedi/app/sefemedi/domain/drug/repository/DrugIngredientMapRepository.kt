package com.safemedi.app.sefemedi.domain.drug.repository

import com.safemedi.app.sefemedi.domain.drug.entity.DrugIngredientMap
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface DrugIngredientMapRepository : JpaRepository<DrugIngredientMap, Long> {
    @Query(
        """
        select dim
        from DrugIngredientMap dim
        join fetch dim.drug d
        join fetch dim.ingredient i
        where d.atcCode in :atcCodes
           or d.drugName in :drugNames
        """
    )
    fun findAllByMedicationKeys(
        @Param("atcCodes") atcCodes: Collection<String>,
        @Param("drugNames") drugNames: Collection<String>,
    ): List<DrugIngredientMap>
}
