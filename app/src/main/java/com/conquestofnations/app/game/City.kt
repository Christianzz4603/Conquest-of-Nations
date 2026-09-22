package com.conquestofnations.app.game

/**
 * A conquerable city -- the base unit of territory in the game (countries
 * are just a grouping for their cities, not directly conquerable). Position
 * is real-world longitude/latitude so it can be projected onto the actual
 * country borders rendered on the map.
 */
data class City(
    val id: String,
    val name: String,
    val countryId: String,
    val lon: Double,
    val lat: Double,
    val adjacentCityIds: List<String>,
    val ownerId: String,
    val garrison: Int
)
