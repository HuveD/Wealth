package kr.co.huve.wealthApp.model.repository.database

import androidx.room.Database
import androidx.room.RoomDatabase
import kr.co.huve.wealthApp.model.repository.database.dao.PlaceDao
import kr.co.huve.wealthApp.model.repository.database.entity.Place

private const val DATABASE_VERSION = 1

@Database(entities = [Place::class], version = DATABASE_VERSION, exportSchema = false)
abstract class WealthDatabase : RoomDatabase() {
    abstract fun placeDao(): PlaceDao
}