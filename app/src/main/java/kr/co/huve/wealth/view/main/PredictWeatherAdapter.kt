package kr.co.huve.wealth.view.main

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kr.co.huve.wealth.R
import kr.co.huve.wealth.model.backend.data.DayWeather
import kr.co.huve.wealth.model.backend.data.Weather
import kr.co.huve.wealth.model.backend.data.WeekWeather
import java.util.*

class PredictWeatherAdapter<T : Weather>(var weathers: List<T>) :
    RecyclerView.Adapter<PredictWeatherAdapter<T>.Holder>() {

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
        holder.title.apply { text = getHour(weather.dt) }
    }

    private fun getHour(dateTime: Long): String {
        val hour = Date().run {
            time = dateTime * 1000L
            ((time % 86400000) / 3600000).toInt()
        }
        return if (hour < 10) "0$hour:00" else "$hour:00"
    }

    private fun getTemp(context: Context, weather: Weather): String {
        return when (weather) {
            is DayWeather -> {
                "${weather.temp.toInt()}${context.getString(R.string.symbol_celsius)}"
            }
            else -> {
                "${(weather as WeekWeather).temp.day.toInt()}${context.getString(R.string.symbol_celsius)}"
            }
        }
    }

    inner class Holder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.predictTitle)
        val icon: ImageView = view.findViewById(R.id.predictIcon)
        val temp: TextView = view.findViewById(R.id.predictTemp)
    }
}