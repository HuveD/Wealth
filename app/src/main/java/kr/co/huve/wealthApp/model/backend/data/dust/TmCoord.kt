package kr.co.huve.wealthApp.model.backend.data.dust

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class TmCoord(
    @SerializedName("list")
    val items: List<TmItem>
) : Serializable

data class TmItem(
    // 도, 시
    val sidoName: String,
    // 구
    val sggName: String,
    // 동
    val umdName: String,

    // TM 좌표계
    val tmX: Double,
    val tmY: Double
) : Serializable