package com.vondi.trackshag.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.vondi.trackshag.ui.components.StepsChart
import com.vondi.trackshag.ui.theme.Blue
import com.vondi.trackshag.ui.theme.LightBlue

@Composable
fun StepsScreen(
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val data = Pair(1000, 10000)

        StepsChart(
            data = data,
            modifier = Modifier
                .padding(top = 50.dp)
                .size(150.dp),
        )
    }

}


