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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.conquestofnations.app.game.City
import com.conquestofnations.app.game.GameState
import com.conquestofnations.app.game.WorldData
import kotlin.math.hypot

/**
 * First playable slice of the world map:
 * - cities drawn as circles, colored by owning country
 * - tap a city you own to select it, then tap an adjacent city to
 *   move troops into it (reinforce if friendly, attack if not)
 * - bottom panel shows the last action's result, whose turn it is,
 *   and an End Turn button
 *
 * This uses placeholder fixed x/y geometry, not real map projections --
 * that comes once the geodata pipeline is in place.
 */
@Composable
fun WorldMapScreen() {
    val gameState = remember {
        GameState(
            countries = WorldData.countries,
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
                cities.forEach { city ->
                    drawCity(
                        city = city,
                        countryColor = gameState.countries[city.countryId]?.color ?: Color.Gray,
                        isSelected = city.id == selectedCityId,
                        canvasWidth = size.width,
                        canvasHeight = size.height
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

private const val CITY_RADIUS = 28f

private fun findTappedCity(
    cities: List<City>,
    tapOffset: Offset,
    canvasWidth: Float,
    canvasHeight: Float
): City? = cities.firstOrNull { city ->
    val center = cityCenter(city, canvasWidth, canvasHeight)
    hypot((tapOffset.x - center.x).toDouble(), (tapOffset.y - center.y).toDouble()) <= CITY_RADIUS
}

private fun cityCenter(city: City, canvasWidth: Float, canvasHeight: Float): Offset =
    Offset(x = city.xFraction * canvasWidth, y = city.yFraction * canvasHeight)

private fun DrawScope.drawCity(
    city: City,
    countryColor: Color,
    isSelected: Boolean,
    canvasWidth: Float,
    canvasHeight: Float
) {
    val center = cityCenter(city, canvasWidth, canvasHeight)

    if (isSelected) {
        drawCircle(color = Color.White, radius = CITY_RADIUS + 6f, center = center)
    }

    drawCircle(color = countryColor, radius = CITY_RADIUS, center = center)

    repeat(city.garrison) { index ->
        val angle = (index * 47) % 360
        val radians = Math.toRadians(angle.toDouble())
        val distance = CITY_RADIUS * 0.55f
        val markerCenter = Offset(
            x = center.x + (distance * Math.cos(radians)).toFloat(),
            y = center.y + (distance * Math.sin(radians)).toFloat()
        )
        drawCircle(color = Color.Black, radius = 3f, center = markerCenter)
    }
}
