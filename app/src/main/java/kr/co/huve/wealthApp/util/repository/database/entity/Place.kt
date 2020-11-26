package kr.co.huve.wealthApp.util.repository.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Place(
    @PrimaryKey
    val uid: Int,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "lat")
    val lat: Double,
    @ColumnInfo(name = "lng")
    val lng: Double,
    @ColumnInfo(name = "city")
    val city: String,
    @ColumnInfo(name = "detail_city")
    val detailCity: String,
    @ColumnInfo(name = "dust_station")
    val dustStation: String
)