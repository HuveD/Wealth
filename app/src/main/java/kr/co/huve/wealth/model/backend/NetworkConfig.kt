package kr.co.huve.wealth.model.backend

object NetworkConfig {

    //region Common setting
    private const val CONNECT_TIMEOUT = 10L
    private const val WRITE_TIMEOUT = 1L
    private const val READ_TIMEOUT = 20L
    //endregion Common setting

    const val WEATHER_API = "https://api.openweathermap.org/data/2.5/"
    // @Todo: Insert your Api key from https://openweathermap.org/. The api key is free.
    private const val WEATHER_KEY = ""
}