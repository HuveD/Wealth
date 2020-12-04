package kr.co.huve.wealthApp.model.repository.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(indices = [Index(value = ["city", "dust_station"], unique = true)])
data class Place(
    @ColumnInfo(name = "lat")
    val lat: Double,
    @ColumnInfo(name = "lng")
    val lng: Double,
    @ColumnInfo(name = "city")
    val city: String,
    @ColumnInfo(name = "dust_station")
    val dustStation: String
) {
    @PrimaryKey(autoGenerate = true)
    var uid: Int = 0
}