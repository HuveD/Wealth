package kr.co.huve.wealth.util

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import kr.co.huve.wealth.model.backend.data.CovidItems
import kr.co.huve.wealth.util.json.CovidItemDeserializer
import javax.inject.Singleton


@Module
@InstallIn(ApplicationComponent::class)
object UtilModule {

    @Provides
    @Singleton
    fun provideGson(): Gson {
        val builder = GsonBuilder()
        builder.registerTypeAdapter(CovidItems::class.java, CovidItemDeserializer())
        return builder.create()
    }
}