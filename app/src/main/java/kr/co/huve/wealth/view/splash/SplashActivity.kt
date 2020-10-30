package kr.co.huve.wealth.view.splash

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.jakewharton.rxbinding4.view.clicks
import dagger.hilt.EntryPoint
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.android.synthetic.main.activity_splash.*
import kr.co.huve.wealth.R
import kr.co.huve.wealth.intent.SplashIntentFactory
import kr.co.huve.wealth.model.SplashState
import kr.co.huve.wealth.view.EventObservable
import kr.co.huve.wealth.view.MainActivity
import kr.co.huve.wealth.view.StateSubscriber
import javax.inject.Inject

@EntryPoint
class SplashActivity : AppCompatActivity(), StateSubscriber<SplashState> {

    @Inject
    lateinit var splashIntentFactory: SplashIntentFactory
    private val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        disposables.clear()
    }

    override fun Observable<SplashState>.subscribeToState(): Disposable {
        return subscribe {
            when(it) {
                is SplashState.Ready -> {
                    Log.d("정현", "State Ready")
                }
                SplashState.Loading -> {
                    onBackPressed()
                    Log.d("정현", "State Loading")
                }
            }
        }
    }

//    override fun Observable<SplashState>.subscribeToState(): Disposable {
//        return ofType<SplashState.Ready>().subscribe {
//            // The Android kind
//            val intent = Intent(this@TasksActivity, AddEditTaskActivity::class.java)
//            startActivity(intent)
//        }
//    }
}