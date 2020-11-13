package kr.co.huve.wealth.view.main

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.FragmentScoped
import kr.co.huve.wealth.R
import kr.co.huve.wealth.model.backend.data.CovidItem
import kr.co.huve.wealth.util.WealthLocationManager
import kr.co.huve.wealth.view.main.adapter.CovidListAdapter
import javax.inject.Inject

@FragmentScoped
class CovidView @Inject constructor(
    @ActivityContext val context: Context,
    private val locationManager: WealthLocationManager
) {
    val view: View = LayoutInflater.from(context).inflate(R.layout.fragment_disaster, null, false)
    var theme: WealthTheme = WealthTheme.CovidSafe
    var isbinded = false
    private val background: ViewGroup
    private val title: TextView
    private val city: TextView
    private val increaseIcon: ImageView
    private val occurCount: TextView
    private val occurCountLabel: TextView
    private val isolationCountLabel: TextView
    private val regionCountLabel: TextView
    private val inflowCountLabel: TextView
    private val isolationCount: TextView
    private val regionCount: TextView
    private val inflowCount: TextView
    private val updateDate: TextView
    private val covidList: RecyclerView
    private val progressBackground: View
    private val progress: ProgressBar
    private val loading: TextView

    init {
        background = view.findViewById(R.id.background)
        title = view.findViewById(R.id.title)
        city = view.findViewById(R.id.city)
        increaseIcon = view.findViewById(R.id.increaseIcon)
        occurCount = view.findViewById(R.id.occurCount)
        occurCountLabel = view.findViewById(R.id.occurCountLabel)
        regionCountLabel = view.findViewById(R.id.regionCountLabel)
        isolationCountLabel = view.findViewById(R.id.isolationCountLabel)
        inflowCountLabel = view.findViewById(R.id.inflowCountLabel)
        isolationCount = view.findViewById(R.id.isolationCount)
        regionCount = view.findViewById(R.id.regionCount)
        inflowCount = view.findViewById(R.id.inflowCount)
        updateDate = view.findViewById(R.id.updateDate)
        covidList = view.findViewById(R.id.covidList)
        progressBackground = view.findViewById(R.id.progressBackground)
        progress = view.findViewById(R.id.progress)
        loading = view.findViewById(R.id.loading)
    }

    fun bind(data: List<CovidItem>) {
        val nationwide = data.first()
        theme = when {
            nationwide.localOccurCount > 300 -> WealthTheme.CovidDanger
            nationwide.localOccurCount > 0 -> WealthTheme.CovidNormal
            else -> WealthTheme.CovidSafe
        }

        val currentCityData = getCurrentCityData(data)
        invalidateData(currentCityData)
        covidList.apply {
            layoutManager = LinearLayoutManager(context).apply {
                orientation = LinearLayoutManager.HORIZONTAL
            }
            adapter = CovidListAdapter(covidList = data)
            setHasFixedSize(true)
        }
        isbinded = true
    }

    private fun invalidateData(covidItem: CovidItem) {
        // 배경
        background.setBackgroundColor(theme.getBackgroundColor(context))
        title.setTextColor(theme.getLabelColor(context))

        // 도시
        city.setTextColor(theme.getLabelColor(context))
        city.text = covidItem.region

        // 도시 증가 수
        increaseIcon.visibility = if (covidItem.increasedCount > 0) View.VISIBLE else View.GONE
        occurCount.text = covidItem.increasedCount.toString()
        occurCount.setTextColor(theme.getFigureColor(context))
        occurCountLabel.setTextColor(theme.getLabelColor(context))

        // 격리자 수
        isolationCount.text = covidItem.isolatingCount.toString()
        isolationCountLabel.setTextColor(theme.getLabelColor(context))
        isolationCount.setTextColor(theme.getFigureColor(context))

        // 지역 감염 수
        regionCount.text = covidItem.localOccurCount.toString()
        regionCountLabel.setTextColor(theme.getLabelColor(context))
        regionCount.setTextColor(theme.getFigureColor(context))

        // 해외 유입 수
        inflowCount.text = covidItem.inflowCount.toString()
        inflowCountLabel.setTextColor(theme.getLabelColor(context))
        inflowCount.setTextColor(theme.getFigureColor(context))

        // 업데이트 시간
        updateDate.text = ("UPDATE: ${covidItem.createDateString.split(" ")[0]}")
        updateDate.setTextColor(theme.getLabelColor(context))
    }

    private fun getCurrentCityData(data: List<CovidItem>): CovidItem {
        var selected = data.first()
        for (i in 0..data.lastIndex) {
            if (locationManager.getCity().contains(data[i].region)) {
                selected = data[i]
                break
            }
        }
        return selected
    }

    fun refreshProgress(show: Boolean) {
        val visibility = if (show) View.VISIBLE else View.GONE
        progressBackground.visibility = visibility
        progress.visibility = visibility
        loading.visibility = visibility
    }
}