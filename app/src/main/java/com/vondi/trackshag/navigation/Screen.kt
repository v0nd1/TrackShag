package com.vondi.trackshag.navigation


sealed class Screen(val route: String) {
    data object Steps : Screen("steps_screen")
    data object Map : Screen("map_screen")
}