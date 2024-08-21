package com.vondi.trackshag

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.vondi.trackshag.ui.screens.MapScreen
import com.vondi.trackshag.ui.theme.TrackShagTheme
import com.yandex.mapkit.MapKitFactory


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        MapKitFactory.setApiKey("53894d06-f050-443c-b2fc-08638ba702f6")
        MapKitFactory.initialize(this)
        setContent {
            TrackShagTheme {
                MapScreen()
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
}








