package kr.co.huve.wealthApp.model.repository.network

import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import kr.co.huve.wealthApp.BuildConfig
import kr.co.huve.wealthApp.model.repository.network.layer.CovidRestApi
import kr.co.huve.wealthApp.model.repository.network.layer.DustRestApi
import kr.co.huve.wealthApp.model.repository.network.layer.KakaoRestApi
import kr.co.huve.wealthApp.model.repository.network.layer.WeatherRestApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object RemoteModule {
    @Provides
    @Singleton
    fun provideWeatherService(gson: Gson): WeatherRestApi {
        val client = OkHttpClient.Builder()
        if (BuildConfig.DEBUG) client.addInterceptor(HttpLoggingInterceptor().also {
            it.level = HttpLoggingInterceptor.Level.HEADERS
        })
        return Retrofit.Builder()
            .client(client.build())
            .baseUrl(NetworkConfig.WEATHER_API)
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build().create(WeatherRestApi::class.java)
    }

    @Provides
    @Singleton
    fun provideCovidService(): CovidRestApi {
        val client = OkHttpClient.Builder()
        if (BuildConfig.DEBUG) client.addInterceptor(HttpLoggingInterceptor().also {
            it.level = HttpLoggingInterceptor.Level.HEADERS
        })
        return Retrofit.Builder()
            .client(client.build())
            .baseUrl(NetworkConfig.COVID_API)
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .addConverterFactory(ScalarsConverterFactory.create())
            .build().create(CovidRestApi::class.java)
    }

    @Provides
    @Singleton
    fun provideDustService(gson: Gson): DustRestApi {
        val client = OkHttpClient.Builder()
        if (BuildConfig.DEBUG) client.addInterceptor(HttpLoggingInterceptor().also {
            it.level = HttpLoggingInterceptor.Level.HEADERS
        })
        return Retrofit.Builder()
            .client(client.build())
            .baseUrl(NetworkConfig.DUST_API)
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build().create(DustRestApi::class.java)
    }

    @Provides
    @Singleton
    fun provideKakaoService(gson: Gson): KakaoRestApi {
        val client = OkHttpClient.Builder()
        if (BuildConfig.DEBUG) client.addInterceptor(HttpLoggingInterceptor().also {
            it.level = HttpLoggingInterceptor.Level.HEADERS
        })
        return Retrofit.Builder()
            .client(client.build())
            .baseUrl(NetworkConfig.KAKAO_API)
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build().create(KakaoRestApi::class.java)
    }
}