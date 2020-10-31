package kr.co.huve.wealth.model.backend.data

import com.google.gson.annotations.SerializedName

data class Weather(
    val coord: Coordinates,
    val weather: List<WeatherInfo>,
    val base: String,
    val main: WeatherInfoDetails,
    val visibility: Int,
    val wind: Wind,
    val clouds: Cloud,
    // Time of data calculation, unix, UTC
    val dt: Long,
    // 도시 아이디
    val id: Int,
    // 도시 이름
    val name: String?,
    val rain: Rain,
    val snow: Snow,
    val sys: System
)

data class Coordinates(
    val lon: Double,
    val lat: Double
)

data class WeatherInfo(
    // 날씨 id
    val id: Int,
    // 날씨 그룹 (눈, 비, 등등)
    val main: String,
    // 요청 언어로 현재 날씨 그룹 상태 간략 설명
    val description: String,
    // 날씨 아이콘
    val icon: String
)

data class WeatherInfoDetails(
    val temp: Double,
    val feels_like: Double,
    val temp_min: Double,
    val temp_max: Double,
    // 대기압 (해수면, 지상 없는 경우)
    val pressure: Int,
    // 습도
    val humidity: Int,
    // 해수면 대기압
    val sea_level: Int,
    // 지상 대기압
    val grnd_level: Int
)

data class Wind(
    val speed: Double,
    val deg: Int,
    // 돌풍
    val gust: Double
)

data class Cloud(
    // 구름 정도
    val all: Int
)

data class Rain(
    @SerializedName("1h") val hour: Double,
    @SerializedName("3h") val three_hour: Double,
)

data class Snow(
    @SerializedName("1h") val hour: Double,
    @SerializedName("3h") val three_hour: Double,
)

data class System(
    val country: String,
    val sunrise: Long,
    val sunset: Long,
)