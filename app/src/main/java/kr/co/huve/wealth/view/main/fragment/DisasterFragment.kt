package kr.co.huve.wealth.view.main.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.schedulers.Schedulers
import kr.co.huve.wealth.model.backend.NetworkConfig
import kr.co.huve.wealth.model.backend.layer.CovidRestApi
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class DisasterFragment : Fragment() {

    @Inject
    lateinit var covidRestApi: CovidRestApi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        covidRestApi.getCovidStatus(NetworkConfig.COVID_KEY, 1, 20, "20201106", "20201107")
            .subscribeOn(Schedulers.io()).subscribe {
                Timber.d(it.toString())
            }
    }
}