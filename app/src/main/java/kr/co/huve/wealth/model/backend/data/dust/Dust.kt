package kr.co.huve.wealth.model.backend.data.dust

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Dust(
    @SerializedName("list")
    val items: List<DustItem>,
    @SerializedName("parm")
    val requestInfo: RequestInfo
) : Serializable

data class DustItem(
    val dataTime: String,
    // 통합 대기 환경 등급 1~4 (좋음, 보통, 나쁨, 매우 나쁨)
    val khaiGrade: Int,
    // 통합 대기 환경 지수
    val khaiValue: Double,

    // 미세먼지(PM10) 24시간 등급 1~4 (좋음, 보통, 나쁨, 매우 나쁨)
    val pm10Grade: Int,
    // 미세먼지(PM10) 1시간 등급 1~4 (좋음, 보통, 나쁨, 매우 나쁨)
    val pm10Grade1h: Int,
    // 미세먼지(PM10) 농도
    val pm10Value: Double,
    // 미세먼지(PM10) 24시간 예측 농도
    val pm10Value24: Double,

    // 미세먼지(PM2.5) 24시간 등급 1~4 (좋음, 보통, 나쁨, 매우 나쁨)
    val pm25Grade: Int,
    // 미세먼지(PM2.5) 1시간 등급 1~4 (좋음, 보통, 나쁨, 매우 나쁨)
    val pm25Grade1h: Int,
    // 미세먼지(PM2.5) 농도
    val pm25Value: Double,
    // 미세먼지(PM2.5) 24시간 예측 농도
    val pm25Value24: Double,

    // 일산화탄소 등급 1~4 (좋음, 보통, 나쁨, 매우 나쁨)
    val coGrade: Int,
    // 일산화탄소 농도
    val coValue: Double,
    // 이산화질소 등급 1~4 (좋음, 보통, 나쁨, 매우 나쁨)
    val no2Grade: Int,
    // 이산화질소 농도
    val no2Value: Double,
    // 오존 등급 1~4 (좋음, 보통, 나쁨, 매우 나쁨)
    val o3Grade: Int,
    // 오존 농도
    val o3Value: Double,
    // 아황산가스 등급 1~4 (좋음, 보통, 나쁨, 매우 나쁨)
    val so2Grade: Int,
    // 아황산가스 농도
    val so2Value: Double,
) : Serializable

data class RequestInfo(
    @SerializedName("stationName")
    val station: String
) : Serializable