package kr.co.huve.wealth.model.backend.layer

import io.reactivex.rxjava3.core.Observable
import retrofit2.http.GET
import retrofit2.http.Query

interface CovidRestApi {
    @GET("/openapi/service/rest/Covid19/getCovid19SidoInfStateJson")
    fun getCovidStatus(
        @Query("serviceKey", encoded = true) key: String,
        @Query("pageNo") page: Int,
        @Query("numOfRows") numOfRows: Int,
        @Query("startCreateDt") startDate: String,
        @Query("endCreateDt") endDate: String
    ): Observable<String>
}