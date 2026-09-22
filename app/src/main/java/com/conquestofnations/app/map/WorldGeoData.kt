package com.conquestofnations.app.map

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** One country's real-world border geometry, in raw (lon, lat) coordinates. */
data class CountryBorder(
    val id: String,
    val name: String,
    val rings: List<List<Pair<Double, Double>>>
)

/**
 * Loads app/src/main/assets/world_countries.geojson (a trimmed subset of the
 * public-domain Natural Earth country boundaries) and parses it into simple
 * ring lists ready for Canvas rendering with an equirectangular projection.
 */
object WorldGeoData {

    fun load(context: Context): List<CountryBorder> {
        val text = context.assets.open("world_countries.geojson").bufferedReader().use { it.readText() }
        val root = JSONObject(text)
        val features = root.getJSONArray("features")
        val result = mutableListOf<CountryBorder>()

        for (i in 0 until features.length()) {
            val feature = features.getJSONObject(i)
            val id = feature.getString("id")
            val name = feature.getJSONObject("properties").getString("name")
            val geometry = feature.getJSONObject("geometry")
            val type = geometry.getString("type")
            val coordinates = geometry.getJSONArray("coordinates")

            val rings = mutableListOf<List<Pair<Double, Double>>>()
            when (type) {
                "Polygon" -> rings.addAll(parsePolygon(coordinates))
                "MultiPolygon" -> {
                    for (p in 0 until coordinates.length()) {
                        rings.addAll(parsePolygon(coordinates.getJSONArray(p)))
                    }
                }
            }
            result.add(CountryBorder(id, name, rings))
        }
        return result
    }

    private fun parsePolygon(coordsArray: JSONArray): List<List<Pair<Double, Double>>> {
        val rings = mutableListOf<List<Pair<Double, Double>>>()
        for (i in 0 until coordsArray.length()) {
            val ring = coordsArray.getJSONArray(i)
            val points = mutableListOf<Pair<Double, Double>>()
            for (j in 0 until ring.length()) {
                val pt = ring.getJSONArray(j)
                points.add(Pair(pt.getDouble(0), pt.getDouble(1)))
            }
            rings.add(points)
        }
        return rings
    }

    /** Equirectangular projection: lon/lat -> canvas pixel coordinates. */
    fun project(lon: Double, lat: Double, canvasWidth: Float, canvasHeight: Float): Pair<Float, Float> {
        val x = ((lon + 180.0) / 360.0 * canvasWidth).toFloat()
        val y = ((90.0 - lat) / 180.0 * canvasHeight).toFloat()
        return Pair(x, y)
    }
}
