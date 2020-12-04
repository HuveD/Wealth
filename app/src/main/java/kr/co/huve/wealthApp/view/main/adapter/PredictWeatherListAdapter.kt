package kr.co.huve.wealthApp.view.main.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kr.co.huve.wealthApp.R
import kr.co.huve.wealthApp.model.repository.data.DayWeather
import kr.co.huve.wealthApp.model.repository.data.Weather
import kr.co.huve.wealthApp.model.repository.data.WeekWeather
import java.util.*
import kotlin.math.roundToInt

class PredictWeatherListAdapter<T : Weather>(var weathers: List<T>) :
    RecyclerView.Adapter<PredictWeatherListAdapter<T>.Holder>() {
    private val calendar: Calendar = Calendar.getInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        LayoutInflater.from(parent.context).inflate(R.layout.item_weather, parent, false).apply {
            return Holder(this)
        }
    }

    override fun getItemCount(): Int {
        return weathers.size
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val weather = weathers[position]
        holder.icon.setImageResource(weather.weatherInfo.first().getWeatherIcon(false))
        holder.temp.apply { text = getTemp(context = context, weather) }
        holder.title.apply { text = getTitle(weather.dt, weather is DayWeather) }

        if (weather is DayWeather) {
            holder.date.text = getDate(dateTime = weather.dt)
            holder.date.visibility = View.VISIBLE
        } else {
            holder.date.visibility = View.INVISIBLE
        }
    }

    private fun getTitle(dateTime: Long, isHour: Boolean): String {
        return if (isHour) {
            calendar.time = Date().apply { time = dateTime * 1000L }
            val calendarHour = calendar.get(Calendar.HOUR_OF_DAY)
            return if (calendarHour < 10) "0${calendarHour}:00" else "$calendarHour:00"
        } else {
            getDate(dateTime)
        }
    }

    private fun getDate(dateTime: Long): String {
        calendar.time = Date().apply { time = dateTime * 1000L }
        return "${calendar.get(Calendar.MONTH) + 1}/${calendar.get(Calendar.DAY_OF_MONTH)}"
    }

    private fun getTemp(context: Context, weather: Weather): String {
        return when (weather) {
            is DayWeather -> {
                "${weather.temp.roundToInt()}${context.getString(R.string.symbol_celsius)}"
            }
            else -> {
                "${(weather as WeekWeather).temp.day.roundToInt()}${context.getString(R.string.symbol_celsius)}"
            }
        }
    }

    inner class Holder(view: View) : RecyclerView.ViewHolder(view) {
        val date: TextView = view.findViewById(R.id.date)
        val title: TextView = view.findViewById(R.id.predictTitle)
        val icon: ImageView = view.findViewById(R.id.predictIcon)
        val temp: TextView = view.findViewById(R.id.predictTemp)
    }
}