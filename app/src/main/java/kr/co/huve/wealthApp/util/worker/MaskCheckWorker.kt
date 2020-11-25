package kr.co.huve.wealthApp.util.worker

import android.content.Context
import androidx.hilt.Assisted
import androidx.hilt.work.WorkerInject
import androidx.work.WorkerParameters
import androidx.work.rxjava3.RxWorker
import androidx.work.workDataOf
import com.google.gson.Gson
import io.reactivex.rxjava3.core.Single
import kr.co.huve.wealthApp.util.repository.network.NetworkConfig
import kr.co.huve.wealthApp.util.repository.network.data.CovidResult
import kr.co.huve.wealthApp.util.repository.network.layer.CovidRestApi
import kr.co.huve.wealthApp.util.NotificationUtil
import kr.co.huve.wealthApp.util.data.DataKey
import org.json.XML
import java.text.SimpleDateFormat
import java.util.*

class MaskCheckWorker @WorkerInject constructor(
    @Assisted val appContext: Context,
    @Assisted val workerParams: WorkerParameters,
    val notificationUtil: NotificationUtil,
    val covidApi: CovidRestApi,
    val gson: Gson
) : RxWorker(appContext, workerParams) {
    private val format = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
    private val calendar = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -1) }

    override fun createWork(): Single<Result> {
        return Single.fromObservable(
            covidApi.getCovidStatus(
                NetworkConfig.COVID_KEY,
                1,
                20,
                format.format(calendar.time),
                format.format(calendar.time)
            ).map {
                val result = gson.fromJson(XML.toJSONObject(it).toString(), CovidResult::class.java)
                val outputData = workDataOf(
                    DataKey.WORK_NEED_MASK.name to needMask(result)
                )
                Result.success(outputData)
            }.onErrorReturn {
                Result.retry()
            }
        )
    }

    private fun needMask(covidResult: CovidResult): Boolean {
        var need = false
        val list = covidResult.getItemList().reversed()
        if (list.isNotEmpty() && list.first().increasedCount > 0) {
            need = true
        }
        return need
    }
}