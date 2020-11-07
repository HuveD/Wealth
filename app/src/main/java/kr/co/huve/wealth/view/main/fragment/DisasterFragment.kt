package kr.co.huve.wealth.view.main.fragment

import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.schedulers.Schedulers
import kr.co.huve.wealth.R
import kr.co.huve.wealth.model.backend.NetworkConfig
import kr.co.huve.wealth.model.backend.data.CovidResult
import kr.co.huve.wealth.model.backend.data.Item
import kr.co.huve.wealth.model.backend.layer.CovidRestApi
import kr.co.huve.wealth.util.WealthLocationManager
import kr.co.huve.wealth.view.main.adapter.CovidListAdapter
import org.json.XML
import timber.log.Timber
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class DisasterFragment : Fragment() {

    @Inject
    lateinit var covidRestApi: CovidRestApi

    @Inject
    lateinit var gson: Gson

    @Inject
    lateinit var locationManager: WealthLocationManager
    lateinit var background: ViewGroup
    lateinit var city: TextView
    lateinit var increaseIcon: ImageView
    lateinit var occurCount: TextView
    lateinit var isolationCount: TextView
    lateinit var regionCount: TextView
    lateinit var inflowCount: TextView
    lateinit var updateDate: TextView
    lateinit var covidList: RecyclerView

    var itemList: List<Item> = arrayListOf()
    var selectedItem: Item? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val calendar = Calendar.getInstance()
        covidRestApi.getCovidStatus(
            NetworkConfig.COVID_KEY,
            1,
            20,
            "${calendar.get(Calendar.YEAR)}${calendar.get(Calendar.MONTH)+1}0${calendar.get(Calendar.DAY_OF_MONTH)-1}",
            "${calendar.get(Calendar.YEAR)}${calendar.get(Calendar.MONTH)+1}0${calendar.get(Calendar.DAY_OF_MONTH)-1}"
        )
            .subscribeOn(Schedulers.io()).subscribe({
                val obj = gson.fromJson(XML.toJSONObject(it).toString(), CovidResult::class.java)
                Timber.d("$obj")
                val location = locationManager.location
                selectedItem = Geocoder(context, Locale.getDefault()).getFromLocation(
                    location.latitude,
                    location.longitude,
                    1
                ).first()?.run {
                    itemList = obj.getItemList()
                    var selected = itemList.first()
                    for (i in 0..itemList.lastIndex) {
                        if (this.adminArea.contains(itemList[i].region)) {
                            selected = itemList[i]
                            break
                        }
                    }
                    selected
                }
                background.post {
                    if (selectedItem != null) invalidateData(selectedItem!!)

                    covidList.apply {
                        setHasFixedSize(true)
                        layoutManager = LinearLayoutManager(context).run {
                            orientation = LinearLayoutManager.HORIZONTAL
                            this
                        }
                        adapter =
                            CovidListAdapter(itemList.reversed())
                    }
                }
            }, {
                Timber.d(it.toString())
            })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_disaster, container, false)
        background = v.findViewById(R.id.background)
        covidList = v.findViewById(R.id.covidList)
        city = v.findViewById(R.id.city)
        increaseIcon = v.findViewById(R.id.increaseIcon)
        occurCount = v.findViewById(R.id.occurCount)
        isolationCount = v.findViewById(R.id.isolationCount)
        regionCount = v.findViewById(R.id.regionCount)
        inflowCount = v.findViewById(R.id.inflowCount)
        updateDate = v.findViewById(R.id.updateDate)
        return v
    }

    private fun invalidateData(item: Item) {
        city.text = item.region
        when {
            item.covidCount > 0 -> {
                increaseIcon.visibility = View.VISIBLE
                occurCount.text = item.increasedCount.toString()
                occurCount.setTextColor(
                    ContextCompat.getColor(
                        context!!, R.color.iconic_red
                    )
                )

                if (item.covidCount > 30) {
                    background.setBackgroundColor(
                        ContextCompat.getColor(
                            context!!, R.color.iconic_warn
                        )
                    )
                } else {
                    background.setBackgroundColor(
                        ContextCompat.getColor(
                            context!!, R.color.iconic_little_warn
                        )
                    )
                }
            }
            else -> {
                increaseIcon.visibility = View.GONE
                occurCount.text = "0"
                occurCount.setTextColor(
                    ContextCompat.getColor(
                        context!!, R.color.iconic_white
                    )
                )
                background.setBackgroundColor(
                    ContextCompat.getColor(
                        context!!, R.color.iconic_safe
                    )
                )
            }
        }

        isolationCount.text = item.isolatingCount.toString()
        isolationCount.setTextColor(
            if (item.isolatingCount > 0)
                ContextCompat.getColor(
                    context!!, R.color.iconic_red
                ) else {
                ContextCompat.getColor(
                    context!!, R.color.iconic_white
                )
            }
        )
        regionCount.text = item.localOccurCount.toString()
        regionCount.setTextColor(
            if (item.localOccurCount > 0)
                ContextCompat.getColor(
                    context!!, R.color.iconic_red
                ) else {
                ContextCompat.getColor(
                    context!!, R.color.iconic_white
                )
            }
        )
        inflowCount.text = item.inflowCount.toString()
        inflowCount.setTextColor(
            if (item.inflowCount > 0)
                ContextCompat.getColor(
                    context!!, R.color.iconic_red
                ) else {
                ContextCompat.getColor(
                    context!!, R.color.iconic_white
                )
            }
        )

        updateDate.text = ("UPDATE: ${item.createDateString.split(" ")[0]}")
    }
}