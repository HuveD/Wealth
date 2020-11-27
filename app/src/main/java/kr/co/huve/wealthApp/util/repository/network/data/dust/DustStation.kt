package kr.co.huve.wealthApp.util.repository.network.data.dust

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class DustStation(
    @SerializedName("list")
    val stations: List<DustStationItem>,
    var isNetwork: Boolean = true
) : Serializable

data class DustStationItem(
    @SerializedName("tm")
    val distance: Float,
    @SerializedName("addr")
    val address: String,
    val stationName: String
) : Serializable