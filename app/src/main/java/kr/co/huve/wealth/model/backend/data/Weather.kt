package kr.co.huve.wealth.model.backend.data

import android.content.Context
import com.google.gson.annotations.SerializedName
import kr.co.huve.wealth.R
import java.io.Serializable
import java.util.*

data class TotalWeather(
    val lat: Double?,
    val lon: Double?
) : Serializable

data class Weather(
    val coord: Coordinates?,
    val weather: List<WeatherInfo>?,
    val base: String?,
    val main: WeatherInfoDetails?,
    val visibility: Int?,
    val wind: Wind?,
    val clouds: Cloud?,
    // Time of data calculation, unix, UTC
    val dt: Long?,
    // 도시 아이디
    val id: Int?,
    // 도시 이름
    val name: String?,
    val rain: Rain?,
    val snow: Snow?,
    val sys: System?
) : Serializable

data class Coordinates(
    val lat: Double,
    val lon: Double
) : Serializable

data class WeatherInfo(
    // 날씨 id
    val id: Int,
    // 날씨 그룹 (눈, 비, 등등)
    val main: String?,
    // 요청 언어로 현재 날씨 그룹 상태 간략 설명
    val description: String?,
    // 날씨 아이콘
    val icon: String?
) : Serializable {

    fun getWeatherIcon(isTitle: Boolean): Int {
        return when (id) {
            // 화산재, 스콜, 황사, 모래 바람, 안개, 연무, 연기 등등
            701, 711, 721, 731, 741, 751, 761, 762, 771, 781 -> if (isTitle) R.drawable.icon_cloud_big else R.drawable.icon_cloud
            // 눈 종류
            600, 601, 602, 611, 612, 613, 615, 616, 620, 621, 622 -> if (isTitle) R.drawable.icon_snow_big else R.drawable.icon_snow
            // 구름 있음
            801, 802 -> if (isTitle) R.drawable.icon_little_cloud_big else R.drawable.icon_little_cloud
            803 -> if (isTitle) R.drawable.icon_cloud_big else R.drawable.icon_cloud
            804 -> if (isTitle) R.drawable.icon_many_cloud_big else R.drawable.icon_many_cloud
            // 맑음
            800 -> if (isTitle) R.drawable.icon_sun_big else R.drawable.icon_sun
            // 이슬비, 소나기, 천둥 번개 등등
            else -> if (isTitle) R.drawable.icon_rain_big else R.drawable.icon_rain
        }
    }

    fun getWeatherDescription(context: Context): Int {
        val packageName: String = context.packageName
        val resId: Int = context.resources.getIdentifier("weather_$id", "string", packageName)
        return if (resId == 0) R.string.weather_not_found else resId
    }
}

data class WeatherInfoDetails(
    val temp: Double?,
    val feels_like: Double?,
    val temp_min: Double?,
    val temp_max: Double?,
    // 대기압 (해수면, 지상 없는 경우)
    val pressure: Int?,
    // 습도
    val humidity: Int?,
    // 해수면 대기압
    val sea_level: Int?,
    // 지상 대기압
    val grnd_level: Int?
) : Serializable

data class Wind(
    val speed: Double?,
    val deg: Int?,
    // 돌풍
    val gust: Double?
) : Serializable

data class Cloud(
    // 구름 정도
    val all: Int?
) : Serializable

data class Rain(
    @SerializedName("1h") val hour: Double?,
    @SerializedName("3h") val three_hour: Double?,
) : Serializable

data class Snow(
    @SerializedName("1h") val hour: Double?,
    @SerializedName("3h") val three_hour: Double?,
) : Serializable

data class System(
    val country: String?,
    val sunrise: Long?,
    val sunset: Long?,
) : Serializable {

    fun getTimeFromSunSetTime(): String {
        val calendar = Calendar.getInstance()
        if (sunset != null) {
            val date = Date(sunset * 1000L)
            calendar.time = date
        }
        return "${calendar.get(Calendar.HOUR_OF_DAY)}:${calendar.get(Calendar.MINUTE)}"
    }

    fun getTimeFromSunRiseTime(): String {
        val calendar = Calendar.getInstance()
        if (sunrise != null) {
            val date = Date(sunrise * 1000L)
            calendar.time = date
        }
        return "${calendar.get(Calendar.HOUR_OF_DAY)}:${calendar.get(Calendar.MINUTE)}"
    }
}