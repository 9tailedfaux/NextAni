package com.refractional.nextani.utils.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class RatedAnime(
    @PrimaryKey val id: Int,
    @ColumnInfo("title") val title: String,
    @ColumnInfo("recommendations") val recommendations: List<Pair<Int, Int>>,
    @ColumnInfo("image") val image: String
)
