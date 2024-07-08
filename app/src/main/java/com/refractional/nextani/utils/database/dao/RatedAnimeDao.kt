package com.refractional.nextani.utils.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.refractional.nextani.utils.database.model.RatedAnime

@Dao
interface RatedAnimeDao {
    @Query("SELECT * FROM ratedanime")
    fun getAll(): List<RatedAnime>

    @Query("SELECT * FROM ratedanime WHERE id IN (:ids)")
    fun getAllByID(ids: IntArray): List<RatedAnime>

    @Query("DELETE FROM ratedanime WHERE 1=1")
    fun deleteAll()

    @Query("DELETE FROM ratedanime WHERE id = (:id)")
    fun deleteId(id: Int)

    @Query("SELECT * FROM ratedanime ORDER BY popularity DESC LIMIT 1")
    fun getMostPopular(): RatedAnime

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAll(vararg ratedAnime: RatedAnime)

    @Delete
    fun delete(ratedAnime: RatedAnime)
}