package kr.co.huve.wealthApp.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import androidx.core.content.ContextCompat
import com.jakewharton.rxrelay3.BehaviorRelay
import dagger.hilt.android.qualifiers.ApplicationContext
import kr.co.huve.wealthApp.R
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@SuppressLint("MissingPermission")
@Singleton
class WealthLocationManager @Inject constructor(
    @ApplicationContext private val context: Context
) : LocationListener {
    companion object {
        private const val DefaultLocationName = "Default"
        const val LAT_SEOUL = 37.532600
        const val LON_SEOUL = 127.024612
    }

    private val locationManager by lazy { context.getSystemService(Context.LOCATION_SERVICE) as (LocationManager) }
    private val locationRelay by lazy { BehaviorRelay.create<Location>() }
    private var initialized = false

    init {
        initialize()
    }

    private fun initialize() {
        Timber.d("Start initialize")
        if (!permissionGranted()) Timber.e("Check Permission") else {
            var lastKnownLocation = Location(DefaultLocationName)
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                initialized = true
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    0,
                    0f,
                    this,
                    Looper.getMainLooper()
                )
                locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)?.run {
                    lastKnownLocation = this
                }
            }
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                initialized = true
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    0,
                    0f,
                    this,
                    Looper.getMainLooper()
                )
                locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)?.run {
                    if (time > lastKnownLocation.time)
                        lastKnownLocation = this
                }
            }

            if (!initialized) {
                locationRelay.accept(lastKnownLocation.apply {
                    latitude = LAT_SEOUL
                    longitude = LON_SEOUL
                })
            } else if (lastKnownLocation.provider != DefaultLocationName) {
                locationRelay.accept(lastKnownLocation)
            }
        }
    }

    fun isAvailableGps(): Boolean {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    fun getLastLocation(): Location {
        return if (locationRelay.hasValue()) {
            locationRelay.value
        } else {
            Location(DefaultLocationName).apply {
                latitude = LAT_SEOUL
                longitude = LON_SEOUL
            }
        }
    }

    fun getLocation(body: (Location) -> Unit) {
        locationRelay.subscribe {
            body(it)
        }.dispose()
    }

    fun getDetailCity(): String {
        val location = getLastLocation()
        return getDetailCity(location.latitude, location.longitude)
    }

    fun getDetailCity(lat: Double, lng: Double): String {
        val address = Geocoder(context, Locale.getDefault()).getFromLocation(
            lat,
            lng,
            5
        )
        return if (address == null || address.isEmpty()) {
            context.getString(R.string.city)
        } else {
            val item = address.first()
            return if (item.thoroughfare.isNullOrEmpty()) {
                item.adminArea
            } else {
                item.thoroughfare
            }
        }
    }

    fun getCity(): String {
        val location = getLastLocation()
        return getCity(location.latitude, location.longitude)
    }

    fun getCity(lat: Double, lng: Double): String {
        val address = Geocoder(context, Locale.getDefault()).getFromLocation(
            lat,
            lng,
            5
        )
        return if (address == null || address.isEmpty() || address.first().adminArea.isNullOrEmpty()) {
            context.getString(R.string.city)
        } else {
            address.first().adminArea
        }
    }

    private fun permissionGranted(): Boolean {
        return (ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED)
    }

    override fun onLocationChanged(location: Location) {
        Timber.d("Location changed from ${location.provider} (${location.latitude},${location.longitude})")
        locationRelay.accept(location)
    }

    override fun onProviderDisabled(provider: String) {
        Timber.d("onProviderDisabled $provider")
        if (initialized) initialized = false
    }

    override fun onProviderEnabled(provider: String) {
        Timber.d("onProviderEnabled $provider")
        if (!initialized) initialize()
    }
}