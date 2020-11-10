package kr.co.huve.wealth.model.backend.layer

import io.reactivex.rxjava3.core.Observable
import kr.co.huve.wealth.model.backend.data.Dust
import retrofit2.http.GET
import retrofit2.http.Query

interface DustRestApi {
    @GET("/openapi/services/rest/MsrstnInfoInqireSvc/getTMStdrCrdnt")
    fun getTransverseMercatorCoordinate(
        @Query("serviceKey") key: String,
        @Query("umdName") city: String,
        @Query("_returnType") returnType: String
    ): Observable<Dust>

    @GET("/openapi/services/rest/MsrstnInfoInqireSvc/getNearbyMsrstnList")
    fun getDustStation(
        @Query("serviceKey") key: String,
        @Query("tmX") tmX: Double,
        @Query("tmY") tmY: Double,
        @Query("_returnType") returnType: String
    ): Observable<Dust>

    @GET("/openapi/services/rest/ArpltnInforInqireSvc/getMsrstnAcctoRltmMesureDnsty")
    fun getNearDustInfo(
        @Query("serviceKey") key: String,
        @Query("numOfRows") numOfRows: Int,
        @Query("pageNo") page: Int,
        @Query("stationName") stationName: String,
        @Query("dataTerm") dataTerm: String,
        @Query("ver") version: String,
        @Query("_returnType") returnType: String
    ): Observable<Dust>


    @GET("/openapi/services/rest/ArpltnInforInqireSvc/getMinuDustFrcstDspth")
    fun getPredictDustInfo(
        @Query("serviceKey") key: String,
        @Query("searchDate") date: String,
        @Query("ver") version: String,
        @Query("_returnType") returnType: String
    ): Observable<Dust>
}