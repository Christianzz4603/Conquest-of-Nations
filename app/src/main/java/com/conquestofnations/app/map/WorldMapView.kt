package com.conquestofnations.app.map

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.conquestofnations.app.game.City
import com.conquestofnations.app.game.GameState
import com.conquestofnations.app.game.WorldData
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.sin

/**
 * The real world map: actual country borders (from assets/world_countries.geojson)
 * projected with an equirectangular projection kept at its correct 2:1 aspect
 * ratio (so countries aren't stretched), with our playable cities drawn on
 * top at their real lon/lat.
 *
 * - the three countries we're playing with (Brazil, Australia, China) are
 *   drawn with a simplified version of their real flag colors/pattern,
 *   tinted by whoever currently owns them
 * - other countries are shown as neutral scenery so the map reads as a
 *   real world map
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
        Box(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            val cities = gameState.cities.values.toList()

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f) // real equirectangular maps are 2:1 -- keeps countries from stretching
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

                countryBorders.forEach { border ->
                    val path = combinedPath(border.rings, width, height)
                    val bounds = projectedBounds(border.rings, width, height)

                    if (border.id in PLAYABLE_COUNTRY_IDS) {
                        clipPath(path) {
                            drawFlag(border.id, bounds)
                        }
                        val ownerColor = countryOwnerColor(border.id, cities)
                        drawPath(path, color = ownerColor.copy(alpha = 0.35f), style = Fill)
                    } else {
                        drawPath(path, color = NEUTRAL_COLOR, style = Fill)
                    }
                    drawPath(path, color = Color(0xFF6B6B6B), style = Stroke(width = 1f))
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

private val PLAYABLE_COUNTRY_IDS = setOf("BRA", "AUS", "CHN")
private val NEUTRAL_COLOR = Color(0xFFD9D2C5)
private val HUMAN_COLOR = Color(0xFF3B82F6)
private val AI_COLOR = Color(0xFFDC2626)

private fun playerColor(ownerId: String): Color = when (ownerId) {
    "human" -> HUMAN_COLOR
    "ai_red" -> AI_COLOR
    else -> NEUTRAL_COLOR
}

private fun countryOwnerColor(countryId: String, cities: List<City>): Color {
    val cityInCountry = cities.firstOrNull { it.countryId == countryId } ?: return NEUTRAL_COLOR
    return playerColor(cityInCountry.ownerId)
}

private fun combinedPath(
    rings: List<List<Pair<Double, Double>>>,
    canvasWidth: Float,
    canvasHeight: Float
): Path {
    val path = Path()
    rings.forEach { ring ->
        ring.forEachIndexed { index, (lon, lat) ->
            val (x, y) = WorldGeoData.project(lon, lat, canvasWidth, canvasHeight)
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
    }
    return path
}

private fun projectedBounds(
    rings: List<List<Pair<Double, Double>>>,
    canvasWidth: Float,
    canvasHeight: Float
): Rect {
    var minX = Float.MAX_VALUE
    var minY = Float.MAX_VALUE
    var maxX = -Float.MAX_VALUE
    var maxY = -Float.MAX_VALUE
    rings.forEach { ring ->
        ring.forEach { (lon, lat) ->
            val (x, y) = WorldGeoData.project(lon, lat, canvasWidth, canvasHeight)
            if (x < minX) minX = x
            if (x > maxX) maxX = x
            if (y < minY) minY = y
            if (y > maxY) maxY = y
        }
    }
    return Rect(minX, minY, maxX, maxY)
}

/** Simplified versions of each playable country's real flag, drawn with primitives. */
private fun DrawScope.drawFlag(countryId: String, bounds: Rect) {
    when (countryId) {
        "BRA" -> drawBrazilFlag(bounds)
        "AUS" -> drawAustraliaFlag(bounds)
        "CHN" -> drawChinaFlag(bounds)
    }
}

private fun DrawScope.drawBrazilFlag(bounds: Rect) {
    drawRect(color = Color(0xFF009C3B), topLeft = bounds.topLeft, size = bounds.size)
    val cx = bounds.center.x
    val cy = bounds.center.y
    val w = bounds.width
    val h = bounds.height
    val diamond = Path().apply {
        moveTo(cx, cy - h * 0.35f)
        lineTo(cx + w * 0.35f, cy)
        lineTo(cx, cy + h * 0.35f)
        lineTo(cx - w * 0.35f, cy)
        close()
    }
    drawPath(diamond, color = Color(0xFFFFDF00))
    drawCircle(color = Color(0xFF002776), radius = min(w, h) * 0.18f, center = Offset(cx, cy))
}

private fun DrawScope.drawAustraliaFlag(bounds: Rect) {
    drawRect(color = Color(0xFF00247D), topLeft = bounds.topLeft, size = bounds.size)
    val cantonW = bounds.width * 0.4f
    val cantonH = bounds.height * 0.4f
    drawRect(color = Color.White, topLeft = bounds.topLeft, size = Size(cantonW, cantonH))
    drawRect(
        color = Color(0xFFCF142B),
        topLeft = Offset(bounds.left + cantonW * 0.35f, bounds.top + cantonH * 0.35f),
        size = Size(cantonW * 0.3f, cantonH * 0.3f)
    )
    val starDotRadius = min(bounds.width, bounds.height) * 0.03f
    listOf(0.62f to 0.3f, 0.78f to 0.5f, 0.66f to 0.72f, 0.88f to 0.64f).forEach { (fx, fy) ->
        drawCircle(
            color = Color.White,
            radius = starDotRadius,
            center = Offset(bounds.left + bounds.width * fx, bounds.top + bounds.height * fy)
        )
    }
}

private fun DrawScope.drawChinaFlag(bounds: Rect) {
    drawRect(color = Color(0xFFDE2910), topLeft = bounds.topLeft, size = bounds.size)
    val starCenter = Offset(bounds.left + bounds.width * 0.2f, bounds.top + bounds.height * 0.25f)
    val starRadius = min(bounds.width, bounds.height) * 0.14f
    drawStar(starCenter, starRadius, Color(0xFFFFDE00))
}

private fun DrawScope.drawStar(center: Offset, radius: Float, color: Color) {
    val path = Path()
    val points = 5
    for (i in 0 until points * 2) {
        val angle = PI / 2 + i * PI / points
        val r = if (i % 2 == 0) radius else radius * 0.4
        val x = center.x + (r * cos(angle)).toFloat()
        val y = center.y - (r * sin(angle)).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    drawPath(path, color = color)
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

    repeat(city.garrison) { index ->
        val angle = (index * 47) % 360
        val radians = Math.toRadians(angle.toDouble())
        val distance = CITY_RADIUS * 1.6
        val markerCenter = Offset(
            x = center.x + (distance * cos(radians)).toFloat(),
            y = center.y + (distance * sin(radians)).toFloat()
        )
        drawCircle(color = Color.Black, radius = 3f, center = markerCenter)
    }
}
