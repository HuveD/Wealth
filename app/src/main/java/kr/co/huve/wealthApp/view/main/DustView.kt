package kr.co.huve.wealthApp.view.main

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.FragmentScoped
import kr.co.huve.wealthApp.R
import kr.co.huve.wealthApp.model.repository.data.dust.DustItem
import kr.co.huve.wealthApp.util.WealthLocationManager
import kr.co.huve.wealthApp.view.main.adapter.DustListAdapter
import javax.inject.Inject
import kotlin.math.max

@FragmentScoped
class DustView @Inject constructor(
    @ActivityContext val context: Context,
    val locationManager: WealthLocationManager
) {
    val view: View = LayoutInflater.from(context).inflate(R.layout.fragment_dust, null, false)
    var theme: WealthTheme = WealthTheme.CovidSafe
    var isbinded = false
    private val progressBackground: View
    private val background: View
    private val tomorrowIcon: ImageView
    private val todayIcon: ImageView
    private val dustList: RecyclerView
    private val progress: ProgressBar
    private val titleIcon: ImageView
    private val updateDate: TextView
    private val dustGrade: TextView
    private val loading: TextView
    private val city: TextView

    init {
        progressBackground = view.findViewById(R.id.progressBackground)
        background = view.findViewById(R.id.background)
        dustList = view.findViewById(R.id.dustList)
        tomorrowIcon = view.findViewById(R.id.tomorrowIcon)
        todayIcon = view.findViewById(R.id.todayIcon)
        titleIcon = view.findViewById(R.id.titleIcon)
        progress = view.findViewById(R.id.progress)
        updateDate = view.findViewById(R.id.updateDate)
        dustGrade = view.findViewById(R.id.dustGrade)
        loading = view.findViewById(R.id.loading)
        city = view.findViewById(R.id.city)
    }

    fun bind(data: DustItem) {
        // 데이터 업데이트
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
        // 종합 지수에 따른 테마 적용
        dustGrade.text = when (dustItem.khaiGrade) {
            1 -> {
                theme = WealthTheme.DustGood
                context.getString(R.string.grade_good)
            }
            2 -> {
                theme = WealthTheme.DustNormal
                context.getString(R.string.grade_normal)
            }
            3 -> {
                theme = WealthTheme.DustBad
                context.getString(R.string.grade_bad)
            }
            4 -> {
                theme = WealthTheme.DustTooBad
                context.getString(R.string.grade_too_bad)
            }
            else -> {
                theme = WealthTheme.DustWorking
                context.getString(R.string.working)
            }
        }

        // 배경
        background.setBackgroundResource(theme.getBackgroundResource())

        // 도시
        city.setTextColor(theme.getLabelColor(context))
        city.text = stationName

        // 타이틀 이미지
        val todayBigIconRes = getGradeIconRes(dustItem.khaiGrade)
        titleIcon.setImageResource(todayBigIconRes)

        // 현재 - 내일 등급 설정
        val todayIconRes = getGradeMiniIconRes(dustItem.khaiGrade)
        val tomorrowIconRes = getGradeMiniIconRes(max(dustItem.pm10Grade, dustItem.pm25Grade))
        tomorrowIcon.setImageResource(tomorrowIconRes)
        todayIcon.setImageResource(todayIconRes)

        // 업데이트 시간
        dustItem.dataTime
        updateDate.text =
            ("@${context.getString(R.string.dust_copyright)}\nUPDATE: ${dustItem.dataTime}")
        updateDate.setTextColor(theme.getLabelColor(context))
    }

    fun requestStateChange(message: String) {
        dustGrade.text = message
    }

    fun refreshProgress(show: Boolean) {
        val visibility = if (show) View.VISIBLE else View.GONE
        progressBackground.visibility = visibility
        progress.visibility = visibility
        loading.visibility = visibility
    }

    private fun getGradeIconRes(grade: Int): Int {
        return when (grade) {
            1 -> R.drawable.icon_happy_big
            2 -> R.drawable.icon_smile_big
            3 -> R.drawable.icon_sad_big
            4 -> R.drawable.icon_sick_big
            else -> R.drawable.icon_work_big
        }
    }

    private fun getGradeMiniIconRes(grade: Int): Int {
        return when (grade) {
            1 -> R.drawable.icon_happy_mini
            2 -> R.drawable.icon_smile_mini
            3 -> R.drawable.icon_sad_mini
            4 -> R.drawable.icon_sick_mini
            else -> R.drawable.icon_work_mini
        }
    }
}