package kr.co.huve.wealthApp.model.backend.data.dust

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class TmCoord(
    val meta: TmMeta,
    @SerializedName("documents")
    val items: List<TmItem>
) : Serializable

data class TmItem(
    // TM 좌표계
    val x: Double,
    val y: Double
) : Serializable

data class TmMeta(
    // TM 좌표계 반환 수
    @SerializedName("total_count")
    val count: Int
) : Serializable