package kr.co.huve.wealth.model.backend

object NetworkConfig {

    //region Common setting
    private const val CONNECT_TIMEOUT = 10L
    private const val WRITE_TIMEOUT = 1L
    private const val READ_TIMEOUT = 20L
    //endregion Common setting

    const val WEATHER_API = "https://api.openweathermap.org/data/2.5/"

    // @Todo: Insert your Api key from https://openweathermap.org/. The api key is free.
    const val WEATHER_KEY = ""

    const val COVID_API = "http://openapi.data.go.kr/"

    // @Todo: Insert your Api key from https://www.data.go.kr/tcs/dss/selectApiDataDetailView.do?publicDataPk=15043376. The api key is free.
    const val COVID_KEY = ""
}