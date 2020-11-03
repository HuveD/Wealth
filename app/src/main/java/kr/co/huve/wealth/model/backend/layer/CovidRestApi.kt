package kr.co.huve.wealth.model.backend.layer

import io.reactivex.rxjava3.core.Observable
import kr.co.huve.wealth.model.backend.data.SingleWeather
import retrofit2.http.GET
import retrofit2.http.Query

interface CovidRestApi {
    @GET("/openapi/service/rest/Covid19/getCovid19InfStateJson")
    fun getCurrentWeather(
        @Query("key") key: String,
        @Query("page") page: Int,
        @Query("numOfRows") numOfRows: Int,
        @Query("startCreateDt") startDate: String,
        @Query("endCreateDt") endDate: String
    ): Observable<Map<String, SingleWeather>>
}