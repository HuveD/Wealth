package kr.co.huve.wealthApp.util.repository.database.dao

import androidx.room.*
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import kr.co.huve.wealthApp.util.repository.database.entity.Place

@Dao
interface PlaceDao {
    @Query("SELECT * from place")
    fun loadAllPlaces(): Observable<Array<Place>>

    @Query("SELECT * from place where city like :city ")
    fun loadNearPlaces(city: String): Observable<Array<Place>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addPlace(data: Place): Completable

    @Update
    fun updateConflictPlace(data: Place): Completable
}