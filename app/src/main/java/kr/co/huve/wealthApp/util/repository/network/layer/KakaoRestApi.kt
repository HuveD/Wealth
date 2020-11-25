package kr.co.huve.wealthApp.util.repository.network.layer

import io.reactivex.rxjava3.core.Observable
import kr.co.huve.wealthApp.util.repository.network.data.dust.TmCoord
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface KakaoRestApi {
    @GET("/v2/local/geo/transcoord.json")
    fun getTransverseMercatorCoordinate(
        @Header("Authorization") auth: String,
        @Query("x") lng: Double,
        @Query("y") lat: Double,
        @Query("output_coord") outputCoordSystem: String
    ): Observable<TmCoord>
}