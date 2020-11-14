package kr.co.huve.wealth.view.main.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kr.co.huve.wealth.R
import kr.co.huve.wealth.model.backend.data.dust.DustItem

class DustListAdapter(private val dustItem: DustItem) :
    RecyclerView.Adapter<DustListAdapter.Holder>() {
    lateinit var context: Context
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        LayoutInflater.from(parent.context).inflate(R.layout.item_dust, parent, false).apply {
            this@DustListAdapter.context = parent.context
            return Holder(this)
        }
    }

    override fun getItemCount(): Int {
        return DustType.getCount()
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val item = DustType.getObject(position)
        if (item != null) {
            val grade = item.getGrade(dustItem)
            holder.gradeIcon.setImageResource(item.getIcon(grade))
            holder.grade.text = context.getString(item.getGradeString(grade))
            holder.type.text = context.getString(item.getType())
        }
    }

    inner class Holder(view: View) : RecyclerView.ViewHolder(view) {
        val type: TextView = view.findViewById(R.id.type)
        val gradeIcon: ImageView = view.findViewById(R.id.gradeIcon)
        val grade: TextView = view.findViewById(R.id.grade)
    }

    private sealed class DustType {
        object FineDust : DustType()        // 미세먼지
        object UltraFineDust : DustType()   // 초미세먼지
        object CarbonMonoxide : DustType()  // 일산화탄소
        object NitrogenDioxide : DustType() // 이산화질소
        object Ozone : DustType()           // 오존
        object SulfurDioxide : DustType()   // 아황산가스

        fun getType(): Int {
            return when (this) {
                FineDust -> R.string.fine_dust
                UltraFineDust -> R.string.ultra_fine_dust
                CarbonMonoxide -> R.string.carbon_monoxide
                NitrogenDioxide -> R.string.nitrogen_dioxide
                Ozone -> R.string.ozone
                SulfurDioxide -> R.string.sulfur_dioxide
            }
        }

        fun getGrade(item: DustItem): Int {
            return when (this) {
                FineDust -> item.pm10Grade1h
                UltraFineDust -> item.pm25Grade1h
                CarbonMonoxide -> item.coGrade
                NitrogenDioxide -> item.no2Grade
                Ozone -> item.o3Grade
                SulfurDioxide -> item.so2Grade
            }
        }

        fun getGradeString(grade: Int): Int {
            return when (grade) {
                1 -> R.string.grade_good
                2 -> R.string.grade_normal
                3 -> R.string.grade_bad
                else -> R.string.grade_too_bad
            }
        }

        fun getIcon(grade: Int): Int {
            return when (grade) {
                1 -> R.drawable.icon_happy
                2 -> R.drawable.icon_smile
                3 -> R.drawable.icon_sad
                4 -> R.drawable.icon_sick
                else -> R.drawable.icon_work
            }
        }

        companion object {
            fun getObject(position: Int): DustType? =
                DustType::class.sealedSubclasses[position].objectInstance

            fun getCount(): Int = DustType::class.sealedSubclasses.size
        }
    }
}