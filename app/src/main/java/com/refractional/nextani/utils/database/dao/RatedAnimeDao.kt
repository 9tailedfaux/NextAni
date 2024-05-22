package com.refractional.nextani.utils.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.refractional.nextani.utils.database.model.RatedAnime

@Dao
interface RatedAnimeDao {
    @Query("SELECT * FROM ratedanime")
    fun getAll(): List<RatedAnime>

    @Query("SELECT * FROM ratedanime WHERE id IN (:ids)")
    fun getAllByID(ids: IntArray): List<RatedAnime>

    @Insert
    fun insertAll(vararg ratedAnime: RatedAnime)

    @Delete
    fun delete(ratedAnime: RatedAnime)
}