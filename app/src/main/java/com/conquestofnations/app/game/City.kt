package com.conquestofnations.app.game

/**
 * A conquerable city -- the base unit of territory in the game (countries
 * are just a grouping/color for their cities, not directly conquerable).
 */
data class City(
    val id: String,
    val name: String,
    val countryId: String,
    val xFraction: Float, // 0f..1f position on the map canvas (placeholder geometry)
    val yFraction: Float,
    val adjacentCityIds: List<String>,
    val ownerId: String,
    val garrison: Int
)
