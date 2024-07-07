package com.refractional.nextani.utils.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity (primaryKeys = ["source", "recc"])
data class AnilistRecc(
    val source: RatedAnime,
    val recc: RatedAnime,
    val rating: Int
)
