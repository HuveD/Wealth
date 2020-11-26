package kr.co.huve.wealthApp.util.repository.database

import androidx.room.Database
import androidx.room.RoomDatabase
import kr.co.huve.wealthApp.util.repository.database.dao.PlaceDao
import kr.co.huve.wealthApp.util.repository.database.entity.Place

private const val DATABASE_VERSION = 1

@Database(entities = [Place::class], version = DATABASE_VERSION)
abstract class WealthDatabase : RoomDatabase(){
    abstract fun placeDao(): PlaceDao
}