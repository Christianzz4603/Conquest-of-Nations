package com.conquestofnations.app.map

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope

/**
 * Early placeholder for the world map screen.
 *
 * Rendering approach: territories are colored by owner, and small
 * "unit" markers are scattered near each territory's center to represent
 * garrison size at a glance -- similar in spirit to the reference
 * screenshot (map colored by faction, army icons showing troop density).
 *
 * This will be replaced by real country/city geometry (from open geodata)
 * once the map-data pipeline is in place.
 */
@Composable
fun WorldMapScreen() {
    val territories = sampleTerritories()
    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            territories.forEach { territory ->
                drawTerritory(territory, size.width, size.height)
            }
        }
    }
}

private fun DrawScope.drawTerritory(territory: Territory, canvasWidth: Float, canvasHeight: Float) {
    val center = Offset(
        x = territory.centerXFraction * canvasWidth,
        y = territory.centerYFraction * canvasHeight
    )
    val baseRadius = 40f

    drawCircle(color = territory.ownerColor, radius = baseRadius, center = center)

    val markerColor = Color.Black
    repeat(territory.garrisonCount) { index ->
        val angle = (index * 47) % 360
        val radians = Math.toRadians(angle.toDouble())
        val distance = baseRadius * 0.9f
        val markerCenter = Offset(
            x = center.x + (distance * Math.cos(radians)).toFloat(),
            y = center.y + (distance * Math.sin(radians)).toFloat()
        )
        drawCircle(color = markerColor, radius = 4f, center = markerCenter)
    }
}
