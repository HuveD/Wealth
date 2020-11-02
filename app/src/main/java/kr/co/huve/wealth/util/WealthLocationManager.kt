package kr.co.huve.wealth.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@SuppressLint("MissingPermission")
@Singleton
class WealthLocationManager @Inject constructor(@ApplicationContext private val context: Context) :
    LocationListener {
    companion object {
        private const val LAT_SEOUL = 37.532600
        private const val LON_SEOUL = 127.024612
    }

    private val locationManager by lazy { context.getSystemService(Context.LOCATION_SERVICE) as (LocationManager) }
    private var initialized = false
    var location = Location("Default")

    init {
        initialize()
    }

    private fun initialize() {
        Timber.d("Start initialize")
        if (!permissionGranted()) Timber.e("Check Permission") else {
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                initialized = true
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    0,
                    0f,
                    this
                )
                locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)?.run {
                    Timber.d("Provided from the network")
                    location.set(this)
                }
            }
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                initialized = true
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, this)
                locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)?.run {
                    Timber.d("Provided from the GPS")
                    location.set(this)
                }
            }
            if (location.provider == "Default") {
                Timber.d("Provided from the default")
                if (location.latitude == 0.0) location.latitude = LAT_SEOUL
                if (location.longitude == 0.0) location.longitude = LON_SEOUL
            }
        }
    }

    fun isAvailableGps(): Boolean {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    fun getLastLocation(): Location {
        if (!initialized) initialize()
        return location
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
        this.location.set(location)
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