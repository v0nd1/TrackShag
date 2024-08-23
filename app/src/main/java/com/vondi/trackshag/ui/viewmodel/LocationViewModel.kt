package com.vondi.trackshag.ui.viewmodel

import android.location.Location
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class LocationViewModel @Inject constructor(

) : ViewModel() {

    private val _locationData = MutableStateFlow<Pair<Location, Int>?>(null)
    val locationData: StateFlow<Pair<Location, Int>?> = _locationData

    fun updateLocationAndSteps(latitude: Double, longitude: Double, steps: Int = 0) {
        Log.d("VIEWMODELSTEP", "$steps")
        _locationData.value = Pair(Location("").apply {
            this.latitude = latitude
            this.longitude = longitude
        }, steps)
    }
}