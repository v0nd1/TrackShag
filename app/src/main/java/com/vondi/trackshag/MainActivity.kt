package com.vondi.trackshag

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.vondi.trackshag.ui.theme.TrackShagTheme
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.mapview.MapView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt


class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        MapKitFactory.setApiKey(BuildConfig.MAPKIT_API_KEY)
        MapKitFactory.initialize(this)
        setContent {
            TrackShagTheme {
                MapWithRealTimeLocation()
            }
        }

    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
    }

    override fun onStop() {
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }



    @Composable
    fun MapWithRealTimeLocation() {
        val context = LocalContext.current
        val mapView = remember { MapView(context) }
        var lastKnownLocation by remember { mutableStateOf<Location?>(null) }
        var steps by remember { mutableIntStateOf(0) }
        val stepThreshold = 1.0
        var isCameraZoomed by remember { mutableStateOf(false) }

        val locationPermissionState = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                startLocationUpdates(context) { location ->
                    lastKnownLocation?.let { lastLocation ->
                        val distance = distanceBetweenPoints(
                            lastLocation.latitude, lastLocation.longitude,
                            location.latitude, location.longitude
                        )
                        if (distance >= stepThreshold) {
                            val stepsToAdd = (distance / stepThreshold).toInt()
                            steps += stepsToAdd
                            Log.d("ShagCheck", "Distance: $distance meters")
                            Log.d("ShagCheck", "Steps: $steps")
                        }
                    }
                    lastKnownLocation = location

                    if (!isCameraZoomed) {
                        updateMapLocation(mapView, location)
                        isCameraZoomed = true
                    } else {
                        mapView.map.mapObjects.addPlacemark(Point(location.latitude, location.longitude))
                    }
                }
            } else {
                Log.e("MapWithRealTimeLocation", "Location permission denied")
            }
        }

        LaunchedEffect(Unit) {
            locationPermissionState.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { mapView }
        )

        Text(
            text = "Пройдено шагов: $steps",
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 50.dp),
            fontWeight = FontWeight.Bold,
            fontSize = 26.sp
        )
    }

    private fun distanceBetweenPoints(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371000
        val lat1Rad = Math.toRadians(lat1)
        val lon1Rad = Math.toRadians(lon1)
        val lat2Rad = Math.toRadians(lat2)
        val lon2Rad = Math.toRadians(lon2)

        val deltaLat = lat2Rad - lat1Rad
        val deltaLon = lon2Rad - lon1Rad

        val a = sin(deltaLat / 2).pow(2) + cos(lat1Rad) * cos(lat2Rad) * sin(deltaLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return r * c
    }

    fun startLocationUpdates(context: Context, onLocationUpdated: (Location) -> Unit) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        val locationRequest = LocationRequest.create().apply {
            interval = 5000
            fastestInterval = 2000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.let {
                    for (location in it.locations) {
                        onLocationUpdated(location)
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

    fun updateMapLocation(mapView: MapView, location: Location) {
        val point = Point(location.latitude, location.longitude)
        val cameraPosition = CameraPosition(point, 20f, 0f, 0f)
        mapView.map.move(cameraPosition, Animation(Animation.Type.SMOOTH, 1f), null)
        Log.e("ShagCheck", "Location updated: ${point.latitude} ${point.longitude}")
        mapView.map.mapObjects.addPlacemark(point)
    }



//    @Composable
//    fun MapWithLocation() {
//        val context = LocalContext.current
//        val mapView = remember { MapView(context) }
//        var steps by remember { mutableIntStateOf(0) }
//
//        val locationPermissionState = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
//            if (isGranted) {
//                getLocationAndUpdateMap(context, mapView)
//            }else {
//                Log.e("MapWithLocation", "Location permission denied")
//            }
//        }
//
//        LaunchedEffect(Unit) {
//            locationPermissionState.launch(Manifest.permission.ACCESS_FINE_LOCATION)
//        }
//
//
//        AndroidView(
//            modifier = Modifier.fillMaxSize(),
//            factory = { mapView }
//        )
//
//        Text(
//            text = "Шагов пройдено: $steps",
//            style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),
//            modifier = Modifier.padding(top = 50.dp, start = 20.dp)
//        )
//
//
//
//    }
//
//
//    @SuppressLint("MissingPermission")
//    private fun updateMapWithPoints(
//        mapView: MapView,
//        onStepsUpdated: (Int) -> Unit // Lambda for updating step count
//    ) {
//        val points = listOf(
//            Point(55.9188307, 37.8124178),
//            Point(55.9188007, 37.8124178),
//            Point(55.9187307, 37.8124178),
//            Point(55.9187307, 37.8120178),
//            Point(55.9187307, 37.8118178),
//            Point(55.9187307, 37.8117178),
//            Point(55.9188307, 37.8117178),
//            Point(55.9188607, 37.8117178),
//            Point(55.9190107, 37.8117178)
//        )
//
//        val thresholdDistance = 1.0 // Distance threshold in meters for step count
//        var currentStep = 0
//
//        CoroutineScope(Dispatchers.Main).launch {
//            if (points.isNotEmpty()) {
//                val firstPoint = points.first()
//                // Move camera to the first point in the list
//                val cameraPosition = CameraPosition(firstPoint, 18f, 0f, 0f)
//                mapView.map.move(cameraPosition, Animation(Animation.Type.SMOOTH, 1f), null)
//                mapView.map.mapObjects.addPlacemark(firstPoint)
//                for (i in points.indices) {
//                    if (i > 0) {
//                        val previousPoint = points[i - 1]
//                        val currentPoint = points[i]
//
//                        // Calculate distance between previous and current point
//                        val distance = distanceBetweenPoints(
//                            previousPoint.latitude, previousPoint.longitude,
//                            currentPoint.latitude, currentPoint.longitude
//                        )
//
//                        // Move camera and add placemark
//                        mapView.map.move(CameraPosition(currentPoint, 18f, 0f, 0f), Animation(Animation.Type.SMOOTH, 1f), null)
//                        mapView.map.mapObjects.addPlacemark(currentPoint)
//
//                        // Check if the distance exceeds the threshold
//                        if (distance > thresholdDistance) {
//                            currentStep += distance.toInt()
//                        }
//
//                        // Update step count
//                        onStepsUpdated(currentStep)
//                        delay(5000)
//                        Log.e("MapWithLocation", "Moved to: ${currentPoint.latitude}, ${currentPoint.longitude}")
//                        Log.e("MapWithLocation", "Steps completed: $currentStep")
//                    }
//                }
//
//            }
//        }
//    }
//
//
//
//    @SuppressLint("MissingPermission")
//    fun updateMapPeriodically(
//        context: Context,
//        mapView: MapView,
//        onStepsUpdated: (Int) -> Unit
//    ) {
//        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
//
//        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
//            .setMinUpdateIntervalMillis(3000)
//            .build()
//
//        var previousLocation: Location? = null
//        var currentStep = 0
//
//        // Запуск корутины
//        CoroutineScope(Dispatchers.Main).launch {
//            val locationCallback = object : LocationCallback() {
//                override fun onLocationResult(locationResult: LocationResult) {
//                    locationResult.locations.forEach { location ->
//                        // Работа с новыми локациями
//                        location?.let {
//                            val distance = previousLocation?.let { prevLoc ->
//                                distanceBetweenPoints(
//                                    prevLoc.latitude, prevLoc.longitude,
//                                    it.latitude, it.longitude
//                                )
//                            } ?: 0.0
//
//                            // Проверка превышения порога
//                            if (distance > 1.0) {
//                                currentStep += distance.roundToInt()
//                            }
//
//                            // Обновление предыдущей локации
//                            previousLocation = it
//
//                            // Создание новой точки и добавление её на карту
//                            val point = Point(it.latitude, it.longitude)
//                            val cameraPosition = CameraPosition(point, 18f, 0f, 0f)
//                            mapView.map.move(cameraPosition, Animation(Animation.Type.SMOOTH, 1f), null)
//                            mapView.map.mapObjects.addPlacemark(point)
//                            onStepsUpdated(currentStep)
//                            Log.e("ShagCheck", "${it.latitude} ${it.longitude}")
//                        } ?: Log.e("ShagCheck", "Location is null")
//                    }
//                }
//            }
//
//            if (ActivityCompat.checkSelfPermission(
//                    context,
//                    Manifest.permission.ACCESS_FINE_LOCATION
//                ) == PackageManager.PERMISSION_GRANTED ||
//                ActivityCompat.checkSelfPermission(
//                    context,
//                    Manifest.permission.ACCESS_COARSE_LOCATION
//                ) == PackageManager.PERMISSION_GRANTED
//            ) {
//                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
//            }
//        }
//    }
//
//    @SuppressLint("MissingPermission")
//    fun updateMapPeriodically2(
//        context: Context,
//        mapView: MapView,
//        onStepsUpdated: (Int) -> Unit
//    ) {
//        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
//        val locationRequest = LocationRequest.create().apply {
//            interval = 5000 // Интервал обновления в миллисекундах
//            fastestInterval = 3000 // Самый быстрый интервал обновления в миллисекундах
//            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
//        }
//        var previousLocation: Location? = null
//        var currentStep = 0
//        // Определение корутины
//        CoroutineScope(Dispatchers.Main).launch {
//            while (true) {
//                try {
//                    val location = fusedLocationClient.lastLocation.await()
//                    location?.let {
//                        val distance = previousLocation?.let { prevLoc ->
//                            distanceBetweenPoints(
//                                prevLoc.latitude, prevLoc.longitude,
//                                it.latitude, it.longitude
//                            )
//                        } ?: 0.0
//
//                        // Check if the distance exceeds the threshold
//                        if (distance > 1.0) {
//                            currentStep += distance.toInt()
//                        }
//
//                        previousLocation = it
//
//
//                        // Создание новой точки и добавление её на карту
//                        val point = Point(it.latitude, it.longitude)
//                        val cameraPosition = CameraPosition(point, 18f, 0f, 0f)
//                        mapView.map.move(cameraPosition, Animation(Animation.Type.SMOOTH, 1f), null)
//                        mapView.map.mapObjects.addPlacemark(point)
//                        onStepsUpdated(currentStep)
//                        Log.e("ShagCheck", "${it.latitude} ${it.longitude}")
//                    } ?: Log.e("ShagCheck", "Location is null")
//                } catch (e: Exception) {
//                    Log.e("ShagCheck", "Failed to get location", e)
//                }
//
//                delay(5000)
//
//            }
//        }
//    }
//@SuppressLint("MissingPermission")
//fun getLocationAndUpdateMap(context: Context, mapView: MapView) {
//    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
//
//    CoroutineScope(Dispatchers.Main).launch {
//        while (true) {
//            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
//                location?.let {
//                    val point = Point(it.latitude, it.longitude)
//                    val cameraPosition = CameraPosition(point, 14f, 0f, 0f)
//                    mapView.map.move(cameraPosition, Animation(Animation.Type.SMOOTH, 1f), null)
//                    Log.e("ShagCheck", "${it.latitude} ${it.longitude}")
//                    mapView.map.mapObjects.addPlacemark(point)
//                } ?: Log.e("MapWithLocation", "Location is null")
//            }.addOnFailureListener { exception ->
//                Log.e("MapWithLocation", "Failed to get location", exception)
//            }
//            delay(5000)
//        }
//    }
//
//
//}
////
//











}








