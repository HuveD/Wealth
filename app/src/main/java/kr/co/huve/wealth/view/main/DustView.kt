package kr.co.huve.wealth.view.main

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.FragmentScoped
import kr.co.huve.wealth.R
import kr.co.huve.wealth.model.backend.data.dust.DustItem
import kr.co.huve.wealth.util.WealthLocationManager
import kr.co.huve.wealth.view.main.adapter.DustListAdapter
import javax.inject.Inject

@FragmentScoped
class DustView @Inject constructor(
    @ActivityContext val context: Context,
    val locationManager: WealthLocationManager
) {
    val view: View = LayoutInflater.from(context).inflate(R.layout.fragment_dust, null, false)
    var theme: WealthTheme = WealthTheme.CovidSafe
    var isbinded = false
    private val progressBackground: View
    private val dustList: RecyclerView
    private val progress: ProgressBar
    private val updateDate: TextView
    private val dustGrade: TextView
    private val loading: TextView
    private val city: TextView

    init {
        progressBackground = view.findViewById(R.id.progressBackground)
        dustList = view.findViewById(R.id.dustList)
        progress = view.findViewById(R.id.progress)
        updateDate = view.findViewById(R.id.updateDate)
        dustGrade = view.findViewById(R.id.dustGrade)
        loading = view.findViewById(R.id.loading)
        city = view.findViewById(R.id.city)
    }

    fun bind(data: DustItem) {
        invalidateData(dustItem = data, stationName = locationManager.getDetailCity())
        dustList.apply {
            layoutManager = LinearLayoutManager(context).apply {
                orientation = LinearLayoutManager.HORIZONTAL
            }
            adapter = DustListAdapter(dustItem = data)
            setHasFixedSize(true)
        }
        isbinded = true
    }

    private fun invalidateData(dustItem: DustItem, stationName: String) {
        // 배경
//        background.setBackgroundColor(theme.getBackgroundColor(context))
//        title.setTextColor(theme.getLabelColor(context))

        // 도시
        city.setTextColor(theme.getLabelColor(context))
        city.text = stationName

        // 종합 지수
        dustGrade.text = when (dustItem.khaiGrade) {
            1 -> context.getString(R.string.grade_good)
            2 -> context.getString(R.string.grade_normal)
            3 -> context.getString(R.string.grade_bad)
            else -> context.getString(R.string.grade_too_bad)
        }

        // 업데이트 시간
        dustItem.dataTime
        updateDate.text = ("UPDATE: ${dustItem.dataTime}")
        updateDate.setTextColor(theme.getLabelColor(context))
    }

    fun refreshProgress(show: Boolean) {
        val visibility = if (show) View.VISIBLE else View.GONE
        progressBackground.visibility = visibility
        progress.visibility = visibility
        loading.visibility = visibility
    }
}