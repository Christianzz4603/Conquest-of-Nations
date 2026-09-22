package com.conquestofnations.app.map

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.conquestofnations.app.game.City
import com.conquestofnations.app.game.GameState
import com.conquestofnations.app.game.WorldData
import kotlin.math.hypot

/**
 * The real world map: actual country borders (from assets/world_countries.geojson)
 * projected with a simple equirectangular projection, with our playable cities
 * drawn on top at their real lon/lat.
 *
 * - countries we're playing with are tinted by owning player's color
 * - other countries are shown as neutral scenery so the map reads as a real world map
 * - tap a city you own, then an adjacent city, to move/attack
 */
@Composable
fun WorldMapScreen() {
    val context = LocalContext.current
    val countryBorders = remember { WorldGeoData.load(context) }

    val gameState = remember {
        GameState(
            initialCities = WorldData.initialCities(),
            players = WorldData.players
        )
    }

    var selectedCityId by remember { mutableStateOf<String?>(null) }
    var lastMessage by remember {
        mutableStateOf("Tap one of your cities, then an adjacent city to move troops.")
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            val cities = gameState.cities.values.toList()

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFBFE3F0))
                    .pointerInput(cities) {
                        detectTapGestures { tapOffset ->
                            val tapped = findTappedCity(cities, tapOffset, size.width.toFloat(), size.height.toFloat())
                                ?: return@detectTapGestures

                            val currentSelection = selectedCityId
                            when {
                                currentSelection == null -> {
                                    if (tapped.ownerId == gameState.currentPlayerId) {
                                        selectedCityId = tapped.id
                                        lastMessage = "Selected ${tapped.name}. Tap an adjacent city."
                                    } else {
                                        lastMessage = "${tapped.name} isn't yours -- select one of your own cities first."
                                    }
                                }
                                currentSelection == tapped.id -> {
                                    selectedCityId = null
                                    lastMessage = "Deselected ${tapped.name}."
                                }
                                else -> {
                                    lastMessage = gameState.moveOrAttack(currentSelection, tapped.id)
                                    selectedCityId = null
                                }
                            }
                        }
                    }
            ) {
                val width = size.width
                val height = size.height

                // Draw every real country's borders. Ones we're playing with are
                // tinted by their current majority owner; everything else is neutral.
                countryBorders.forEach { border ->
                    val fillColor = countryFillColor(border.id, cities)
                    border.rings.forEach { ring ->
                        val path = pathForRing(ring, width, height)
                        drawPath(path, color = fillColor, style = Fill)
                        drawPath(path, color = Color(0xFF6B6B6B), style = Stroke(width = 1f))
                    }
                }

                cities.forEach { city ->
                    drawCity(
                        city = city,
                        isSelected = city.id == selectedCityId,
                        canvasWidth = width,
                        canvasHeight = height
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(16.dp)
        ) {
            Text(text = lastMessage)
            Text(
                text = "Turn: ${gameState.players.first { it.id == gameState.currentPlayerId }.name}",
                modifier = Modifier.padding(top = 4.dp)
            )
            Button(
                onClick = {
                    gameState.endTurn()
                    selectedCityId = null
                    lastMessage = "Turn ended."
                },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("End Turn")
            }
        }
    }
}

private val NEUTRAL_COLOR = Color(0xFFD9D2C5)
private val HUMAN_COLOR = Color(0xFF3B82F6)
private val AI_COLOR = Color(0xFFDC2626)

private fun playerColor(ownerId: String): Color = when (ownerId) {
    "human" -> HUMAN_COLOR
    "ai_red" -> AI_COLOR
    else -> NEUTRAL_COLOR
}

/** Countries we're actually playing with are tinted by whoever owns their first city. */
private fun countryFillColor(countryId: String, cities: List<City>): Color {
    val cityInCountry = cities.firstOrNull { it.countryId == countryId } ?: return NEUTRAL_COLOR
    return playerColor(cityInCountry.ownerId)
}

private fun pathForRing(ring: List<Pair<Double, Double>>, canvasWidth: Float, canvasHeight: Float): Path {
    val path = Path()
    ring.forEachIndexed { index, (lon, lat) ->
        val (x, y) = WorldGeoData.project(lon, lat, canvasWidth, canvasHeight)
        if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    return path
}

private const val CITY_RADIUS = 16f

private fun cityCenter(city: City, canvasWidth: Float, canvasHeight: Float): Offset {
    val (x, y) = WorldGeoData.project(city.lon, city.lat, canvasWidth, canvasHeight)
    return Offset(x, y)
}

private fun findTappedCity(
    cities: List<City>,
    tapOffset: Offset,
    canvasWidth: Float,
    canvasHeight: Float
): City? = cities.firstOrNull { city ->
    val center = cityCenter(city, canvasWidth, canvasHeight)
    hypot((tapOffset.x - center.x).toDouble(), (tapOffset.y - center.y).toDouble()) <= CITY_RADIUS + 8f
}

private fun DrawScope.drawCity(
    city: City,
    isSelected: Boolean,
    canvasWidth: Float,
    canvasHeight: Float
) {
    val center = cityCenter(city, canvasWidth, canvasHeight)
    val color = playerColor(city.ownerId)

    if (isSelected) {
        drawCircle(color = Color.White, radius = CITY_RADIUS + 5f, center = center)
    }

    drawCircle(color = Color.Black, radius = CITY_RADIUS + 2f, center = center)
    drawCircle(color = color, radius = CITY_RADIUS, center = center)

    // One small marker per unit garrisoned here
    repeat(city.garrison) { index ->
        val angle = (index * 47) % 360
        val radians = Math.toRadians(angle.toDouble())
        val distance = CITY_RADIUS * 1.6
        val markerCenter = Offset(
            x = center.x + (distance * Math.cos(radians)).toFloat(),
            y = center.y + (distance * Math.sin(radians)).toFloat()
        )
        drawCircle(color = Color.Black, radius = 3f, center = markerCenter)
    }
}
