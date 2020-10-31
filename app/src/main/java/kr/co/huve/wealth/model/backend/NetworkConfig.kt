package kr.co.huve.wealth.model.backend

import javax.inject.Singleton

@Singleton
object NetworkConfig {
    val WEATHER_API = "https://api.openweathermap.org/data/2.5/"
    // @Todo: Insert your Api key from https://openweathermap.org/. The api key is free.
    val WEATHER_KEY = ""
}