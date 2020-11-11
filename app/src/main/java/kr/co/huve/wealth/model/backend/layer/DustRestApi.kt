package kr.co.huve.wealth.model.backend.layer

import io.reactivex.rxjava3.core.Observable
import kr.co.huve.wealth.model.backend.data.dust.Dust
import kr.co.huve.wealth.model.backend.data.dust.DustStation
import kr.co.huve.wealth.model.backend.data.dust.TmCoord
import retrofit2.http.GET
import retrofit2.http.Query

interface DustRestApi {
    @GET("/openapi/services/rest/MsrstnInfoInqireSvc/getTMStdrCrdnt")
    fun getTransverseMercatorCoordinate(
        @Query("serviceKey", encoded = true) key: String,
        @Query("umdName") city: String,
        @Query("_returnType") returnType: String
    ): Observable<TmCoord>

    @GET("/openapi/services/rest/MsrstnInfoInqireSvc/getNearbyMsrstnList")
    fun getDustStation(
        @Query("serviceKey", encoded = true) key: String,
        @Query("numOfRows") numOfRows: Int,
        @Query("pageNo") page: Int,
        @Query("tmX") tmX: Double,
        @Query("tmY") tmY: Double,
        @Query("_returnType") returnType: String
    ): Observable<DustStation>

    @GET("/openapi/services/rest/ArpltnInforInqireSvc/getMsrstnAcctoRltmMesureDnsty")
    fun getNearDustInfo(
        @Query("serviceKey", encoded = true) key: String,
        @Query("numOfRows") numOfRows: Int,
        @Query("pageNo") page: Int,
        @Query("stationName") stationName: String,
        @Query("dataTerm") dataTerm: String,
        @Query("ver") version: String,
        @Query("_returnType") returnType: String
    ): Observable<Dust>

    @GET("/openapi/services/rest/ArpltnInforInqireSvc/getMinuDustFrcstDspth")
    fun getPredictDustInfo(
        @Query("serviceKey", encoded = true) key: String,
        @Query("searchDate") date: String,
        @Query("ver") version: String,
        @Query("_returnType") returnType: String
    ): Observable<Dust>
}