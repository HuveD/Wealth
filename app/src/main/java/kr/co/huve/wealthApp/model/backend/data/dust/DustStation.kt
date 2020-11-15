package kr.co.huve.wealthApp.model.backend.data.dust

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class DustStation(
    @SerializedName("list")
    val stations: List<DustStationItem>
) : Serializable

data class DustStationItem(
    @SerializedName("tm")
    val distance: Float,
    @SerializedName("addr")
    val address: String,
    val stationName: String
) : Serializable