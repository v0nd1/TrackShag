package com.vondi.trackshag.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.vondi.trackshag.ui.theme.Blue
import com.vondi.trackshag.ui.theme.LightBlue

@Composable
fun StepsChart(
    data: Pair<Int, Int>,
    modifier: Modifier = Modifier
) {
    val (startValue, endValue) = data
    val total = endValue.toDouble()
    val currentValue = startValue.coerceIn(0, endValue).toDouble()
    val filledPercentage = (currentValue / total) * 360f

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 60f

            drawArc(
                color = LightBlue,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                size = Size(size.width, size.height),
                style = Stroke(width = strokeWidth)
            )

            drawArc(
                color = Blue,
                startAngle = -90f,
                sweepAngle = filledPercentage.toFloat(),
                useCenter = false,
                size = Size(size.width, size.height),
                style = Stroke(width = strokeWidth)
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize(Alignment.Center)
        ) {
            Text(
                text = "$startValue",
                textAlign = TextAlign.Center
            )
        }
    }


}