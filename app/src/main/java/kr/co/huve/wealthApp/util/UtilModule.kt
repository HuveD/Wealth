package kr.co.huve.wealthApp.util

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import kr.co.huve.wealthApp.util.json.CovidItemDeserializer
import kr.co.huve.wealthApp.util.json.DoubleDeserializer
import kr.co.huve.wealthApp.util.json.IntDeserializer
import kr.co.huve.wealthApp.util.repository.network.data.CovidItems
import javax.inject.Singleton


@Module
@InstallIn(ApplicationComponent::class)
object UtilModule {

    @Provides
    @Singleton
    fun provideGson(): Gson {
        val builder = GsonBuilder()
        builder.registerTypeAdapter(CovidItems::class.java, CovidItemDeserializer())
        builder.registerTypeAdapter(Double::class.java, DoubleDeserializer())
        builder.registerTypeAdapter(Int::class.java, IntDeserializer())
        return builder.create()
    }
}