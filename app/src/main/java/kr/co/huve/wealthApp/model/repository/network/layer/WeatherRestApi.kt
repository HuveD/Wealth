package kr.co.huve.wealthApp.model.repository.network.layer

import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import kr.co.huve.wealthApp.model.repository.data.SingleWeather
import kr.co.huve.wealthApp.model.repository.data.TotalWeather
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherRestApi {
    @GET("/data/2.5/weather")
    fun getCurrentWeatherWithCity(
        @Query("appid") key: String,
        @Query("q") city: String,
        @Query("lang") lang: String,
        @Query("units") units: String
    ): Maybe<SingleWeather>

    @GET("/data/2.5/weather")
    fun getCurrentWeatherWithCoords(
        @Query("appid") key: String,
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("lang") lang: String,
        @Query("units") units: String
    ): Maybe<SingleWeather>

    @GET("/data/2.5/onecall")
    fun getTotalWeatherWithCoords(
        @Query("appid") key: String,
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("exclude") exclude: String,
        @Query("lang") lang: String,
        @Query("units") units: String
    ): Maybe<TotalWeather>
}