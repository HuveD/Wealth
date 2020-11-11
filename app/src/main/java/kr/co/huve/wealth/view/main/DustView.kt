package kr.co.huve.wealth.view.main

import android.content.Context
import android.location.Geocoder
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
import kr.co.huve.wealth.model.backend.data.Item
import kr.co.huve.wealth.util.WealthLocationManager
import kr.co.huve.wealth.view.main.adapter.CovidListAdapter
import java.util.*
import javax.inject.Inject

@FragmentScoped
class DustView @Inject constructor(
    @ActivityContext val context: Context,
    private val locationManager: WealthLocationManager
) {
    val view: View = LayoutInflater.from(context).inflate(R.layout.fragment_dust, null, false)
    var theme: WealthTheme = WealthTheme.CovidSafe
    var isbinded = false

    init {

    }

    fun bind(data: List<Item>) {
        isbinded = true
    }
}