package kr.co.huve.wealth.model.backend

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import kr.co.huve.wealth.model.backend.layer.CovidRestApi
import kr.co.huve.wealth.model.backend.layer.WeatherRestApi
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object RemoteModule {
    @Provides
    @Singleton
    fun provideWeatherService(): WeatherRestApi {
        return Retrofit.Builder()
            .baseUrl(NetworkConfig.WEATHER_API)
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(WeatherRestApi::class.java)
    }

    @Provides
    @Singleton
    fun provideCovidService(): CovidRestApi {
        return Retrofit.Builder()
            .baseUrl(NetworkConfig.COVID_API)
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(CovidRestApi::class.java)
    }
}