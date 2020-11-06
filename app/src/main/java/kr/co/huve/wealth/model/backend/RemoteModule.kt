package kr.co.huve.wealth.model.backend

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import kr.co.huve.wealth.BuildConfig
import kr.co.huve.wealth.model.backend.layer.CovidRestApi
import kr.co.huve.wealth.model.backend.layer.WeatherRestApi
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
    fun provideWeatherService(): WeatherRestApi {
        val client = OkHttpClient.Builder()
        if (BuildConfig.DEBUG) client.addInterceptor(HttpLoggingInterceptor().also {
            it.level = HttpLoggingInterceptor.Level.HEADERS
        })
        return Retrofit.Builder()
            .client(client.build())
            .baseUrl(NetworkConfig.WEATHER_API)
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
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
}