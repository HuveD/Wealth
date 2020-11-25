package kr.co.huve.wealthApp.util.repository.network.data

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class CovidResult(
    @SerializedName("response")
    val response: Response
) : Serializable {
    fun getItemList(): List<CovidItem> {
        return if (response.body?.result != null) {
            response.body.result.covidItems
        } else emptyList()
    }
}

data class Response(
    @SerializedName("header")
    val header: Header,
    @SerializedName("body")
    val body: Body?
) : Serializable

data class Header(
    @SerializedName("resultCode")
    val resultCode: String,
    @SerializedName("resultMsg")
    val resultMsg: String
) : Serializable

data class Body(
    @SerializedName("items")
    val result: CovidItems?,
    @SerializedName("numOfRows")
    val numOfRows: Int,
    @SerializedName("pageNo")
    val pageNo: Int,
    @SerializedName("totalCount")
    val totalCount: Int
) : Serializable

data class CovidItems(
    @SerializedName("item")
    val covidItems: List<CovidItem>
) : Serializable

data class CovidItem(
    // 데이터 생성 날짜
    @SerializedName("createDt")
    val createDateString: String,
    // 해당 지역 사망자 수
    @SerializedName("deathCnt")
    val deathCount: Int,
    // 해당 지역 확진자 수
    @SerializedName("defCnt")
    val covidCount: Int,
    // 해당 지역명
    @SerializedName("gubun")
    val region: String,
    // 전일 대비 증감 수
    @SerializedName("incDec")
    val increasedCount: Int,
    // 격리 해제 수
    @SerializedName("isolClearCnt")
    val isolationDoneCount: Int,
    // 격리 수
    @SerializedName("isolIngCnt")
    val isolatingCount: Int,
    // 지역 발생 수
    @SerializedName("localOccCnt")
    val localOccurCount: Int,
    // 유입 발생
    @SerializedName("overFlowCnt")
    val inflowCount: Int,
    // 10만명 당 발생 비율
    @SerializedName("qurRate")
    val occurrencePerTenThousand: String,
) : Serializable