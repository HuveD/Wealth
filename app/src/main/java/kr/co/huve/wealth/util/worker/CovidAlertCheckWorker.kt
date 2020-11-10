package kr.co.huve.wealth.util.worker

import android.content.Context
import androidx.hilt.Assisted
import androidx.hilt.work.WorkerInject
import androidx.work.WorkerParameters
import androidx.work.rxjava3.RxWorker
import androidx.work.workDataOf
import com.google.gson.Gson
import io.reactivex.rxjava3.core.Single
import kr.co.huve.wealth.model.backend.NetworkConfig
import kr.co.huve.wealth.model.backend.data.CovidResult
import kr.co.huve.wealth.model.backend.layer.CovidRestApi
import kr.co.huve.wealth.util.data.DataKey
import org.json.XML
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class CovidAlertCheckWorker @WorkerInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    var covidApi: CovidRestApi,
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
                val need = needMask(result)
                val outputData = workDataOf(
                    DataKey.WORK_NEED_MASK.name to need
                )
                Timber.d("Do I have to take a mask? -> $need")
                Result.success(outputData)
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