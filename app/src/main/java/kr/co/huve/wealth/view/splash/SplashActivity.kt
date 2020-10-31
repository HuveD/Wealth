package kr.co.huve.wealth.view.splash

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import kr.co.huve.wealth.R
import kr.co.huve.wealth.intent.SplashIntentFactory
import kr.co.huve.wealth.model.SplashModelStore
import kr.co.huve.wealth.model.SplashState
import kr.co.huve.wealth.view.EventObservable
import kr.co.huve.wealth.view.MainActivity
import kr.co.huve.wealth.view.StateSubscriber
import javax.inject.Inject


@AndroidEntryPoint
class SplashActivity : AppCompatActivity(),
    StateSubscriber<SplashState>,
    EventObservable<SplashViewEvent> {
    @Inject
    lateinit var splashIntentFactory: SplashIntentFactory

    @Inject
    lateinit var splashModelStore: SplashModelStore
    private val disposables = CompositeDisposable()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
    }

    override fun onResume() {
        super.onResume()
        // 모델 상태 관찰. 상태 변경에 따라 subscribeToState에서 로직 분리
        disposables.add(splashModelStore.modelState().subscribeToState())
        // 이벤트 발생 관찰. 이벤트 발생시 이벤트 팩토리에 정의된 액션 시작
        disposables.add(events().subscribe(splashIntentFactory::process))
    }

    override fun onPause() {
        super.onPause()
        disposables.clear()
    }

    override fun Observable<SplashState>.subscribeToState(): Disposable {
        return subscribe {
            Log.d("정현", it.toString())
            if (it is SplashState.Ready) {
                Intent(
                    this@SplashActivity,
                    MainActivity::class.java
                ).apply { startActivity(this) }
            }
        }
    }

    override fun events(): Observable<SplashViewEvent> {
        return splashModelStore.modelState().filter { it == SplashState.Idle }.map {
            // Idle 상태시 로딩 요청 이벤트 전달
            SplashViewEvent.RequestWeatherFromActivity
        }
    }
}