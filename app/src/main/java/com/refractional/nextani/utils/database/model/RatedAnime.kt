package com.refractional.nextani.utils.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class RatedAnime(
    @PrimaryKey val id: Int,
    val rating: Int
)
