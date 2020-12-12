package kr.co.huve.wealthApp.view.main.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kr.co.huve.wealthApp.R
import kr.co.huve.wealthApp.model.repository.data.CovidItem

class CovidListAdapter(var covidList: List<CovidItem>, val onClick: (CovidItem) -> Unit) :
    RecyclerView.Adapter<CovidListAdapter.Holder>() {

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
        holder.parent.setOnClickListener {
            onClick(item)
        }
    }

    inner class Holder(view: View) : RecyclerView.ViewHolder(view) {
        val parent: View = view
        val region: TextView = view.findViewById(R.id.region)
        val total: TextView = view.findViewById(R.id.total)
        val increase: TextView = view.findViewById(R.id.increase)
    }
}