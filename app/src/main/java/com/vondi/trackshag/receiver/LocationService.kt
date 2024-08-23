package com.vondi.trackshag.receiver

import android.Manifest
import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.vondi.trackshag.R
import com.vondi.trackshag.ui.viewmodel.LocationViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

@AndroidEntryPoint
class LocationService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var lastKnownLocation: Location? = null



    private val tag = "LocationService"

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundService()
        startLocationUpdates()
        return START_STICKY
    }

    @SuppressLint("ForegroundServiceType")
    private fun startForegroundService() {
        val notification = NotificationCompat.Builder(this, "LocationChannel")
            .setContentTitle("Location Service")
            .setContentText("Tracking your location")
            .build()

        startForeground(1, notification)
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 60000)
            .setMinUpdateIntervalMillis(60000)
            .build()

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        }
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val newLocation = locationResult.lastLocation
            if (newLocation != null) {
                val steps = lastKnownLocation?.let { calculateSteps(it, newLocation) }

                // Обновляем ViewModel с последней локацией и шагами
                if (steps != null) {
                    //locationViewModel.updateLocationAndSteps(newLocation.latitude, newLocation.longitude, steps)
                }

                // Обновляем последнюю известную локацию
                lastKnownLocation = newLocation
            }
        }
    }

    private fun calculateSteps(lastLocation: Location, newLocation: Location): Int {
        val r = 6371000 // Радиус Земли в метрах
        val lat1Rad = Math.toRadians(lastLocation.latitude)
        val lon1Rad = Math.toRadians(lastLocation.longitude)
        val lat2Rad = Math.toRadians(newLocation.latitude)
        val lon2Rad = Math.toRadians(newLocation.longitude)

        val deltaLat = lat2Rad - lat1Rad
        val deltaLon = lon2Rad - lon1Rad

        // Формула Хаверсина https://en.wikipedia.org/wiki/Haversine_formula
        val a = sin(deltaLat / 2).pow(2) + cos(lat1Rad) * cos(lat2Rad) * sin(deltaLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val distance = r * c
        var steps = 0
        val stepThreshold = 1.0
        if (distance >= stepThreshold) {
            val stepsToAdd = (distance / stepThreshold).toInt()
            steps += stepsToAdd
            Log.d(tag, "Distance: $distance meters \n Steps: $steps")
        }

        return steps
    }

    override fun onBind(intent: Intent?): IBinder? = null
}