package kr.co.huve.wealthApp.util

import android.content.Context
import androidx.work.WorkManager
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import kr.co.huve.wealthApp.model.repository.data.CovidItems
import kr.co.huve.wealthApp.model.repository.json.CovidItemDeserializer
import kr.co.huve.wealthApp.model.repository.json.DoubleDeserializer
import kr.co.huve.wealthApp.model.repository.json.IntDeserializer
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

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }
}