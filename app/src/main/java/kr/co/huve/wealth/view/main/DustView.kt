package kr.co.huve.wealth.view.main

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.FragmentScoped
import kr.co.huve.wealth.R
import kr.co.huve.wealth.model.backend.data.dust.DustItem
import kr.co.huve.wealth.util.WealthLocationManager
import javax.inject.Inject

@FragmentScoped
class DustView @Inject constructor(
    @ActivityContext val context: Context,
    private val locationManager: WealthLocationManager
) {
    val view: View = LayoutInflater.from(context).inflate(R.layout.fragment_dust, null, false)
    var theme: WealthTheme = WealthTheme.CovidSafe
    var isbinded = false
    private val progressBackground: View
    private val progress: ProgressBar
    private val loading: TextView

    init {
        progressBackground = view.findViewById(R.id.progressBackground)
        progress = view.findViewById(R.id.progress)
        loading = view.findViewById(R.id.loading)
    }

    fun bind(data: List<DustItem>) {
        isbinded = true
    }

    fun refreshProgress(show: Boolean) {
        val visibility = if (show) View.VISIBLE else View.GONE
        progressBackground.visibility = visibility
        progress.visibility = visibility
        loading.visibility = visibility
    }
}