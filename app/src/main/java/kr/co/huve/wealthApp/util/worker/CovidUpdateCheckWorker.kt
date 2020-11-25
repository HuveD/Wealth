package kr.co.huve.wealthApp.util.worker

import android.content.Context
import androidx.hilt.Assisted
import androidx.hilt.work.WorkerInject
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.rxjava3.RxWorker
import com.google.gson.Gson
import io.reactivex.rxjava3.core.Single
import kr.co.huve.wealthApp.util.repository.network.NetworkConfig
import kr.co.huve.wealthApp.util.repository.network.data.CovidResult
import kr.co.huve.wealthApp.util.repository.network.layer.CovidRestApi
import kr.co.huve.wealthApp.util.NotificationUtil
import kr.co.huve.wealthApp.util.data.DataKey
import kr.co.huve.wealthApp.util.data.NotificationRes
import org.json.XML
import java.text.SimpleDateFormat
import java.util.*

class CovidUpdateCheckWorker @WorkerInject constructor(
    @Assisted val appContext: Context,
    @Assisted val workerParams: WorkerParameters,
    private val notificationUtil: NotificationUtil,
    private val covidApi: CovidRestApi,
    private val gson: Gson
) :
    RxWorker(appContext, workerParams) {
    private val format = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
    private val calendar = Calendar.getInstance()

    override fun createWork(): Single<Result> {
        return Single.fromObservable(
            covidApi.getCovidStatus(
                NetworkConfig.COVID_KEY,
                1,
                20,
                format.format(calendar.time),
                format.format(calendar.time)
            ).map {
                val result =
                    gson.fromJson(XML.toJSONObject(it).toString(), CovidResult::class.java)
                if (result.getItemList().isNotEmpty()) {
                    // Show notification
                    notificationUtil.makeNotification(
                        NotificationRes.CovidUpdate(
                            appContext,
                            result.getItemList().reversed().first().increasedCount
                        )
                    )

                    // Cancel period work
                    WorkManager.getInstance(appContext)
                        .cancelUniqueWork(DataKey.WORK_COVID_UPDATED.name)
                }
                Result.success()
            }.onErrorReturn {
                Result.retry()
            }
        )
    }
}