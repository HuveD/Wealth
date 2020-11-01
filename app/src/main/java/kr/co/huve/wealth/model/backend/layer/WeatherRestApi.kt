package kr.co.huve.wealth.model.backend.layer

import io.reactivex.rxjava3.core.Observable
import kr.co.huve.wealth.model.backend.data.TotalWeather
import kr.co.huve.wealth.model.backend.data.Weather
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherRestApi {
    @GET("/data/2.5/weather")
    fun getCurrentWeatherWithCity(
        @Query("appid") key: String,
        @Query("q") city: String,
        @Query("lang") lang: String,
        @Query("units") units: String
    ): Observable<Weather>

    @GET("/data/2.5/weather")
    fun getCurrentWeatherWithCoords(
        @Query("appid") key: String,
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("lang") lang: String,
        @Query("units") units: String
    ): Observable<Weather>

    @GET("/data/2.5/oncall")
    fun getTotalWeatherWithCoords(
        @Query("appid") key: String,
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("exclude") exclude: String,
        @Query("lang") lang: String,
        @Query("units") units: String
    ): Observable<TotalWeather>
}