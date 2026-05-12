package com.safemedi.app.sefemedi.domain.drug.entity

import jakarta.persistence.*

@Entity
@Table(name = "ingredient_master")
class IngredientMaster(
    @Id
    @Column(name = "ingredient_code", length = 50)
    val ingredientCode: String,

    @Column(name = "ingredient_name")
    var ingredientName: String? = null
)
