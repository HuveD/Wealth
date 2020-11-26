package kr.co.huve.wealthApp.util.repository.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import io.reactivex.rxjava3.core.Observable
import kr.co.huve.wealthApp.util.repository.database.entity.Place

@Dao
interface PlaceDao {
    @Query("SELECT * from place")
    fun loadAllPlaces(): Observable<Array<Place>>

    @Insert
    fun addPlace(user: Place)
}