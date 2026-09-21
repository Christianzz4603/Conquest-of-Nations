package com.conquestofnations.app.game

import androidx.compose.ui.graphics.Color

/**
 * First playable slice: a small hand-placed set of countries/cities.
 * This will be replaced by data generated from real-world geodata
 * (100+ countries, realistic per-country city counts) once the
 * map-data pipeline is built.
 */
object WorldData {

    val players = listOf(
        Player(id = "human", name = "You", isAi = false),
        Player(id = "ai_red", name = "Red AI", isAi = true)
    )

    val countries = listOf(
        Country(id = "usa", name = "United States", color = Color(0xFF3B6EA5)),
        Country(id = "bra", name = "Brazil", color = Color(0xFF4C9F5A)),
        Country(id = "rus", name = "Russia", color = Color(0xFFB33B3B))
    ).associateBy { it.id }

    fun initialCities(): Map<String, City> {
        val human = players[0].id
        val ai = players[1].id

        val list = listOf(
            City("us_ny", "New York", "usa", 0.20f, 0.32f, listOf("us_dc", "us_chi"), human, 5),
            City("us_dc", "Washington", "usa", 0.19f, 0.35f, listOf("us_ny", "us_chi"), human, 4),
            City("us_chi", "Chicago", "usa", 0.16f, 0.30f, listOf("us_ny", "us_dc"), human, 3),

            City("br_sp", "Sao Paulo", "bra", 0.30f, 0.68f, listOf("br_rio", "br_bsb"), human, 3),
            City("br_rio", "Rio de Janeiro", "bra", 0.31f, 0.70f, listOf("br_sp", "br_bsb"), human, 2),
            City("br_bsb", "Brasilia", "bra", 0.29f, 0.64f, listOf("br_sp", "br_rio"), human, 2),

            City("ru_msc", "Moscow", "rus", 0.62f, 0.20f, listOf("ru_spb", "ru_nsk"), ai, 6),
            City("ru_spb", "Saint Petersburg", "rus", 0.60f, 0.16f, listOf("ru_msc", "ru_nsk"), ai, 3),
            City("ru_nsk", "Novosibirsk", "rus", 0.72f, 0.22f, listOf("ru_msc", "ru_spb"), ai, 3)
        )
        return list.associateBy { it.id }
    }
}
