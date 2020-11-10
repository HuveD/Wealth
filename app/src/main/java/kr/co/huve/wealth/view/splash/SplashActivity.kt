package kr.co.huve.wealth.view.splash

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.jakewharton.rxrelay3.PublishRelay
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import kr.co.huve.wealth.R
import kr.co.huve.wealth.intent.SplashIntentFactory
import kr.co.huve.wealth.model.backend.data.TotalWeather
import kr.co.huve.wealth.model.splash.SplashModelStore
import kr.co.huve.wealth.model.splash.SplashState
import kr.co.huve.wealth.util.TaskManager
import kr.co.huve.wealth.util.WealthLocationManager
import kr.co.huve.wealth.util.data.DataKey
import kr.co.huve.wealth.view.EventObservable
import kr.co.huve.wealth.view.StateSubscriber
import kr.co.huve.wealth.view.main.WealthActivity
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class SplashActivity : AppCompatActivity(),
    StateSubscriber<SplashState>,
    EventObservable<SplashViewEvent> {
    @Inject
    lateinit var splashIntentFactory: SplashIntentFactory

    @Inject
    lateinit var splashModelStore: SplashModelStore

    @Inject
    lateinit var locationManager: WealthLocationManager

    @Inject
    lateinit var taskManager: TaskManager

    private val permissionRelay = PublishRelay.create<SplashViewEvent>()
    private val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        setContentView(R.layout.activity_splash)
    }

    override fun onResume() {
        super.onResume()
        // 이벤트 발생 관찰. 이벤트 발생시 이벤트 팩토리에 정의된 액션 시작
        disposables.add(events().subscribe(splashIntentFactory::process))
        // 모델 상태 관찰. 상태 변경에 따라 subscribeToState에서 로직 분리
        disposables.add(splashModelStore.modelState().subscribeToState())
    }

    override fun onPause() {
        super.onPause()
        // 관찰 해제
        disposables.clear()
    }

    override fun Observable<SplashState>.subscribeToState(): Disposable {
        return subscribe {
            Timber.d("State Changed: $it")
            when (it) {
                is SplashState.Idle -> checkPermission()
                is SplashState.RequestPermission -> {
                    // 퍼미션 요청
                    if (!it.consumed) {
                        requestPermission(it.permission)
                        it.consumed = true
                    } else {
                        // 요청 완료 후 모든 권한이 허용되었는지 체크
                        checkPermission()
                    }
                }
                // 권한 체크 완료 -> 현재 위치 날씨 조회 중
                is SplashState.Loading -> taskManager.scheduleMorningAlert()
                is SplashState.Error -> errorOccurred()
                is SplashState.Complete -> startWealth(it.dataSet)
            }
        }
    }

    override fun events(): Observable<SplashViewEvent> {
        return permissionRelay
    }

    private fun checkPermission() {
        return when {
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED -> {
                permissionRelay.accept(SplashViewEvent.CheckPermission(Manifest.permission.ACCESS_FINE_LOCATION))
            }
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED -> {
                permissionRelay.accept(SplashViewEvent.CheckPermission(Manifest.permission.ACCESS_COARSE_LOCATION))
            }
            else -> {
                // 모든 퍼미션 허용됨
                Timber.d("All permission granted")
                permissionRelay.accept(SplashViewEvent.PermissionGranted)
            }
        }
    }

    private fun requestPermission(permission: String) {
        ActivityCompat.requestPermissions(
            this@SplashActivity,
            arrayOf(permission),
            0
        )
    }

    private fun startWealth(singleWeather: TotalWeather) {
        // 로딩 완료
        if (!locationManager.isAvailableGps())
            Toast.makeText(this, getString(R.string.turn_on_gps), Toast.LENGTH_SHORT).show()

        // Start wealth activity
        startActivity(Intent(this@SplashActivity, WealthActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra(DataKey.EXTRA_WEATHER_DATA.name, singleWeather)
        })
    }

    private fun errorOccurred() {
        Toast.makeText(
            this@SplashActivity,
            getString(R.string.error_load_app),
            Toast.LENGTH_SHORT
        ).show()
        finish()
    }
}