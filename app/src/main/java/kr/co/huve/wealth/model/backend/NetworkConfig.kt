package kr.co.huve.wealth.model.backend

object NetworkConfig {

    //region Common setting
    private const val CONNECT_TIMEOUT = 10L
    private const val WRITE_TIMEOUT = 1L
    private const val READ_TIMEOUT = 20L
    const val RETRY = 3L
    //endregion Common setting

    // @Todo: Insert your Api key from https://openweathermap.org/. The api key is free.
    const val WEATHER_API = "https://api.openweathermap.org/data/2.5/"
    const val WEATHER_KEY = ""

    // @Todo: Insert your Api key from https://www.data.go.kr/tcs/dss/selectApiDataDetailView.do?publicDataPk=15043376. The api key is free.
    const val COVID_API = "http://openapi.data.go.kr/"
    const val COVID_KEY = ""

    // @Todo: Insert your Api key from https://www.data.go.kr/iim/api/selectAPIAcountView.do. The api key is free.
    const val DUST_API = "http://openapi.airkorea.or.kr/"
    const val DUST_KEY = ""
    const val DUST_PREDICT_API_VERSION = "1.1"
    const val DUST_API_VERSION = "1.3"
}