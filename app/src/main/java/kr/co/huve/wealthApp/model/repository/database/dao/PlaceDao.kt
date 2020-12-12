package kr.co.huve.wealthApp.model.repository.database.dao

import androidx.room.*
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import kr.co.huve.wealthApp.model.repository.database.entity.Place

@Dao
interface PlaceDao {
    @Query("SELECT * from place")
    fun loadAllPlaces(): Maybe<Array<Place>>

    @Query("SELECT * from place where city like :city ")
    fun loadNearPlaces(city: String): Maybe<Array<Place>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addPlace(data: Place): Completable

    @Update
    fun updateConflictPlace(data: Place): Completable
}