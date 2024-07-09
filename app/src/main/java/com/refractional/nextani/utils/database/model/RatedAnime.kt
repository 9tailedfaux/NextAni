package com.refractional.nextani.utils.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class RatedAnime(
    @PrimaryKey val id: Int,
    val rating: Int?,
    val avgScore: Int,
    val status: String?,
    val type: String,
    val format: String,
    val title: String,
    val popularity: Int,
    val year: Int,
    val airStatus: String,
    val imgUrl: String,
    val color: String,
    val episodes: Int
)
