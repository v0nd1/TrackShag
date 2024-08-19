package com.vondi.trackshag

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.vondi.trackshag.ui.theme.TrackShagTheme
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {
    private lateinit var mapView: MapView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var placemark: PlacemarkMapObject? = null
    private var stepCount = 0
    private var lastLocation: Location? = null
    private lateinit var locationCallback: LocationCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapKitFactory.setApiKey("YOUR_API_KEY_HERE")
        MapKitFactory.initialize(this)
        setContent {
            TrackShagTheme {
                StepCounterWithMap()
            }
        }
    }

    @Composable
    fun StepCounterWithMap() {
        val context = LocalContext.current

        mapView = remember { MapView(context) }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        // Проверка разрешений
        val locationPermissionState = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                startLocationUpdates(context)
            } else {
                // Обработка отказа в разрешении
            }
        }

        LaunchedEffect(Unit) {
            locationPermissionState.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { mapView },
            update = { mapView ->
                if (placemark == null) {
                    placemark = mapView.map.mapObjects.addPlacemark(
                        Point(0.0, 0.0),
                        ImageProvider.fromResource(context, R.drawable.arrow)
                    ).apply {
                        setIconStyle(
                            IconStyle().apply {
                                scale = 1.5f
                            }
                        )
                    }
                }
            }
        )

        Column {
            Text(text = "Steps: $stepCount")

            Button(onClick = { startSimulatedMovement(context) }) {
                Text("Start Simulated Movement")
            }
            Button(onClick = { stopLocationUpdates() }) {
                Text("Stop Tracking")
            }
        }
    }

    private fun startLocationUpdates(context: Context) {
        val locationRequest = LocationRequest.create().apply {
            interval = 5000
            fastestInterval = 2000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                p0.let {
                    for (location in it.locations) {
                        updateLocationOnMap(location)
                        updateStepCount(location)
                    }
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        }
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    @Composable
    private fun startSimulatedMovement() {
        // Пример точек для симуляции
        val points = listOf(
            Point(55.751244, 37.618423), // Москва
            Point(55.752244, 37.618423), // Слегка измененная позиция
            Point(55.753244, 37.618423)  // Еще одна измененная позиция
        )

        val coroutineScope = rememberCoroutineScope()

        LaunchedEffect(Unit) {
            coroutineScope.launch {
                for (point in points) {
                    val location = Location("simulated").apply {
                        latitude = point.latitude
                        longitude = point.longitude
                    }
                    updateLocationOnMap(location)
                    updateStepCount(location)
                    delay(3000) // Задержка для демонстрации перемещения
                }
            }
        }
    }

    private fun updateLocationOnMap(location: Location) {
        val point = Point(location.latitude, location.longitude)
        placemark?.let {
            it.geometry = point
            val cameraPosition = CameraPosition(point, 14f, 0f, 0f)
            mapView.map.move(cameraPosition, Animation(Animation.Type.SMOOTH, 1f), null)
        }
    }

    private fun updateStepCount(location: Location) {
        if (lastLocation != null) {
            val distance = lastLocation!!.distanceTo(location)
            if (distance > STEP_THRESHOLD) {
                stepCount += (distance / STEP_LENGTH).toInt()
            }
        }
        lastLocation = location
    }

    companion object {
        private const val STEP_THRESHOLD = 5f // Минимальное расстояние для шага
        private const val STEP_LENGTH = 0.7f // Средняя длина шага в метрах
    }
}









//
//@Composable
//fun MapWithLocation() {
//    val context = LocalContext.current
//    val mapView = remember { MapView(context) }
//
//    val locationPermissionState = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
//        if (isGranted) {
//            getLocationAndUpdateMap(context, mapView)
//        } else {
//            Log.e("MapWithLocation", "Location permission denied")
//        }
//    }
//
//    LaunchedEffect(Unit) {
//        locationPermissionState.launch(Manifest.permission.ACCESS_FINE_LOCATION)
//    }
//
//    AndroidView(
//        modifier = Modifier.fillMaxSize(),
//        factory = { mapView }
//    )
//}
//
//@SuppressLint("MissingPermission")
//fun getLocationAndUpdateMap(context: Context, mapView: MapView) {
//    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
//
//    fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
//        location?.let {
//            val point = Point(it.latitude, it.longitude)
//            val cameraPosition = CameraPosition(point, 14f, 0f, 0f)
//            mapView.map.move(cameraPosition, Animation(Animation.Type.SMOOTH, 1f), null)
//            mapView.map.mapObjects.addPlacemark(point)
//            Log.e("MapWithLocation", "${location.latitude} ${location.longitude} ${location.time}")
//        } ?: Log.e("MapWithLocation", "Location is null")
//    }.addOnFailureListener { exception ->
//        Log.e("MapWithLocation", "Failed to get location", exception)
//    }
//
//
//}


