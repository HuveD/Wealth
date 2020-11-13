package kr.co.huve.wealth.view.main.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kr.co.huve.wealth.R
import kr.co.huve.wealth.model.backend.data.dust.DustItem

class DustListAdapter(var dustList: DustItem) :
    RecyclerView.Adapter<DustListAdapter.Holder>() {

    private sealed class DustListItem {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        LayoutInflater.from(parent.context).inflate(R.layout.item_dust, parent, false).apply {
            return Holder(this)
        }
    }

    override fun getItemCount(): Int {
        return 0
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
//        val item = dustList[position]
//        holder.type.text = item.holder.gradeIcon.text = item.covidCount.toString()
//        holder.grade.text = item.increasedCount.toString()
    }

    inner class Holder(view: View) : RecyclerView.ViewHolder(view) {
        val type: TextView = view.findViewById(R.id.type)
        val gradeIcon: TextView = view.findViewById(R.id.gradeIcon)
        val grade: TextView = view.findViewById(R.id.grade)
    }
}