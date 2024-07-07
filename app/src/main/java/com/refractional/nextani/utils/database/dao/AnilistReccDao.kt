package com.refractional.nextani.utils.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.refractional.nextani.utils.database.model.AnilistRecc
import com.refractional.nextani.utils.database.model.RatedAnime

@Dao
interface AnilistReccDao {
    @Query("SELECT * FROM anilistrecc")
    fun getAll(): List<RatedAnime>

    @Query("SELECT * FROM anilistrecc WHERE id IN (:ids)")
    fun getAllByID(ids: IntArray): List<RatedAnime>

    @Query("DELETE FROM anilistrecc WHERE 1=1")
    fun deleteAll()

    @Query("DELETE FROM anilistrecc WHERE source = (:source) AND recc = (:recc)")
    fun deleteId(source: Int, recc: Int)

    @Insert
    fun insertAll(vararg anilistRecc: AnilistRecc)

    @Delete
    fun delete(anilistRecc: AnilistRecc)
}