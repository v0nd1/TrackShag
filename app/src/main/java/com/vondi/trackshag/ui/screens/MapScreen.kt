package com.vondi.trackshag.ui.screens

import android.Manifest
import android.location.Location
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.mapview.MapView
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.yandex.mapkit.Animation
import com.yandex.mapkit.map.CameraPosition
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun MapScreen() {
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



private fun startLocationUpdates(context: Context, onLocationUpdated: (Location) -> Unit) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
            .setMinUpdateIntervalMillis(3000)
            .build()

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

private fun updateMapLocation(mapView: MapView, location: Location) {

    val point = Point(location.latitude, location.longitude)
    val cameraPosition = CameraPosition(point, 20f, 0f, 0f)
    mapView.map.move(cameraPosition, Animation(Animation.Type.SMOOTH, 1f), null)
    Log.e("ShagCheck", "Location updated: ${point.latitude} ${point.longitude}")
    mapView.map.mapObjects.addPlacemark(point)

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
