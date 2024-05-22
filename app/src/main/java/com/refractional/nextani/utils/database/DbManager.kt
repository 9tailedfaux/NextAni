package com.refractional.nextani.utils.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.refractional.nextani.utils.database.dao.RatedAnimeDao
import com.refractional.nextani.utils.database.model.RatedAnime

@Database(entities = [RatedAnime::class], version = 1)
abstract class DbManager : RoomDatabase() {
    abstract fun ratedAnimeDao(): RatedAnimeDao
}