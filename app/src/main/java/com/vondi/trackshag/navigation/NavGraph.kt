package com.vondi.trackshag.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.vondi.trackshag.ui.screens.MapScreen
import com.vondi.trackshag.ui.screens.StepsScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Steps.route
    ) {
        composable(route = Screen.Steps.route){
            StepsScreen()
        }
        composable(route = Screen.Map.route){
            MapScreen()
        }

    }
}