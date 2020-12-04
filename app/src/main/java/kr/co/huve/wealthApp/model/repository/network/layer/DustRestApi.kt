package kr.co.huve.wealthApp.model.repository.network.layer

import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import kr.co.huve.wealthApp.model.repository.data.dust.Dust
import kr.co.huve.wealthApp.model.repository.data.dust.DustStation
import retrofit2.http.GET
import retrofit2.http.Query

interface DustRestApi {

    @GET("/openapi/services/rest/MsrstnInfoInqireSvc/getNearbyMsrstnList")
    fun getDustStation(
        @Query("serviceKey", encoded = true) key: String,
        @Query("numOfRows") numOfRows: Int,
        @Query("pageNo") page: Int,
        @Query("tmX") tmX: Double,
        @Query("tmY") tmY: Double,
        @Query("_returnType") returnType: String
    ): Maybe<DustStation>

    @GET("/openapi/services/rest/ArpltnInforInqireSvc/getMsrstnAcctoRltmMesureDnsty")
    fun getNearDustInfo(
        @Query("serviceKey", encoded = true) key: String,
        @Query("numOfRows") numOfRows: Int,
        @Query("pageNo") page: Int,
        @Query("stationName") stationName: String,
        @Query("dataTerm") dataTerm: String,
        @Query("ver") version: String,
        @Query("_returnType") returnType: String
    ): Maybe<Dust>

    @GET("/openapi/services/rest/ArpltnInforInqireSvc/getMinuDustFrcstDspth")
    fun getPredictDustInfo(
        @Query("serviceKey", encoded = true) key: String,
        @Query("searchDate") date: String,
        @Query("ver") version: String,
        @Query("_returnType") returnType: String
    ): Maybe<Dust>
}