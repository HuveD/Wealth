package kr.co.huve.wealth.view.main.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kr.co.huve.wealth.R
import kr.co.huve.wealth.model.backend.data.DayWeather
import kr.co.huve.wealth.model.backend.data.Item
import kr.co.huve.wealth.model.backend.data.Weather
import kr.co.huve.wealth.model.backend.data.WeekWeather
import java.util.*

class CovidListAdapter<T : Item>(var covidList: List<T>) :
    RecyclerView.Adapter<CovidListAdapter<T>.Holder>() {
    private val calendar: Calendar = Calendar.getInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        LayoutInflater.from(parent.context).inflate(R.layout.item_covid, parent, false).apply {
            return Holder(this)
        }
    }

    override fun getItemCount(): Int {
        return covidList.size
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val item = covidList[position]
        holder.region.text = item.region
        holder.total.text = item.covidCount.toString()
        holder.increase.text = item.increasedCount.toString()
    }

    inner class Holder(view: View) : RecyclerView.ViewHolder(view) {
        val region: TextView = view.findViewById(R.id.region)
        val total: TextView = view.findViewById(R.id.total)
        val increase: TextView = view.findViewById(R.id.increase)
    }
}