package kr.co.huve.wealthApp.util.repository.database

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import kr.co.huve.wealthApp.util.repository.database.dao.PlaceDao
import javax.inject.Singleton

@InstallIn(ApplicationComponent::class)
@Module
object DatabaseModule {

    @Provides
    fun providePlaceDao(wealthDatabase: WealthDatabase): PlaceDao {
        return wealthDatabase.placeDao()
    }

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext appContext: Context): WealthDatabase {
        return Room.databaseBuilder(
            appContext,
            WealthDatabase::class.java,
            "wealth_db"
        ).build()
    }
}