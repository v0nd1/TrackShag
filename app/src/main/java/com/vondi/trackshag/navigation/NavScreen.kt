package com.vondi.trackshag.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController

@Composable
fun NavScreen() {
    val navController = rememberNavController()

    Scaffold (
        bottomBar = {

        }
    ) {
        Box(modifier = Modifier.padding(it)){
            NavGraph(navController = navController)
        }

    }
}