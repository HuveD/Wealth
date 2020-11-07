package kr.co.huve.wealth.view.main.fragment

import android.content.Context
import android.location.Geocoder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.FragmentScoped
import kr.co.huve.wealth.R
import kr.co.huve.wealth.model.backend.data.Item
import kr.co.huve.wealth.util.WealthLocationManager
import kr.co.huve.wealth.view.main.adapter.CovidListAdapter
import java.util.*
import javax.inject.Inject

@FragmentScoped
class CovidView @Inject constructor(
    @ActivityContext val context: Context,
    val locationManager: WealthLocationManager
) {
    val view: View = LayoutInflater.from(context).inflate(R.layout.fragment_disaster, null, false)
    private val background: ViewGroup
    private val city: TextView
    private val increaseIcon: ImageView
    private val occurCount: TextView
    private val isolationCount: TextView
    private val regionCount: TextView
    private val inflowCount: TextView
    private val updateDate: TextView
    private val covidList: RecyclerView
    private val progress: ProgressBar

    init {
        background = view.findViewById(R.id.background)
        city = view.findViewById(R.id.city)
        increaseIcon = view.findViewById(R.id.increaseIcon)
        occurCount = view.findViewById(R.id.occurCount)
        isolationCount = view.findViewById(R.id.isolationCount)
        regionCount = view.findViewById(R.id.regionCount)
        inflowCount = view.findViewById(R.id.inflowCount)
        updateDate = view.findViewById(R.id.updateDate)
        covidList = view.findViewById(R.id.covidList)
        progress = view.findViewById(R.id.progress)
    }

    fun bind(data: List<Item>) {
        val currentCityData = getCurrentCityData(data)
        invalidateData(currentCityData)
        covidList.apply {
            layoutManager = LinearLayoutManager(context).apply {
                orientation = LinearLayoutManager.HORIZONTAL
            }
            adapter = CovidListAdapter(covidList = data)
            setHasFixedSize(true)
        }
    }

    private fun invalidateData(item: Item) {
        city.text = item.region
        when {
            item.covidCount > 0 -> {
                increaseIcon.visibility = View.VISIBLE
                occurCount.text = item.increasedCount.toString()
                occurCount.setTextColor(
                    ContextCompat.getColor(
                        context, R.color.iconic_red
                    )
                )

                if (item.covidCount > 30) {
                    background.setBackgroundColor(
                        ContextCompat.getColor(
                            context, R.color.iconic_warn
                        )
                    )
                } else {
                    background.setBackgroundColor(
                        ContextCompat.getColor(
                            context, R.color.iconic_little_warn
                        )
                    )
                }
            }
            else -> {
                increaseIcon.visibility = View.GONE
                occurCount.text = "0"
                occurCount.setTextColor(
                    ContextCompat.getColor(
                        context, R.color.iconic_white
                    )
                )
                background.setBackgroundColor(
                    ContextCompat.getColor(
                        context, R.color.iconic_safe
                    )
                )
            }
        }

        isolationCount.text = item.isolatingCount.toString()
        isolationCount.setTextColor(
            if (item.isolatingCount > 0)
                ContextCompat.getColor(
                    context, R.color.iconic_red
                ) else {
                ContextCompat.getColor(
                    context, R.color.iconic_white
                )
            }
        )
        regionCount.text = item.localOccurCount.toString()
        regionCount.setTextColor(
            if (item.localOccurCount > 0)
                ContextCompat.getColor(
                    context, R.color.iconic_red
                ) else {
                ContextCompat.getColor(
                    context, R.color.iconic_white
                )
            }
        )
        inflowCount.text = item.inflowCount.toString()
        inflowCount.setTextColor(
            if (item.inflowCount > 0)
                ContextCompat.getColor(
                    context, R.color.iconic_red
                ) else {
                ContextCompat.getColor(
                    context, R.color.iconic_white
                )
            }
        )
        updateDate.text = ("UPDATE: ${item.createDateString.split(" ")[0]}")
    }

    private fun getCurrentCityData(data: List<Item>): Item {
        val location = locationManager.location
        var selected = data.first()
        Geocoder(context, Locale.getDefault()).getFromLocation(
            location.latitude,
            location.longitude,
            1
        ).first()?.also {
            for (i in 0..data.lastIndex) {
                if (it.adminArea.contains(data[i].region)) {
                    selected = data[i]
                    break
                }
            }
        }
        return selected
    }

    fun refreshProgress(show: Boolean) {
        progress.visibility = if (show) View.VISIBLE else View.GONE
    }
}