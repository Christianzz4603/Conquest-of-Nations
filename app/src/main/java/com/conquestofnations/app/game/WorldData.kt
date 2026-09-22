package com.conquestofnations.app.game

/**
 * First playable slice: three real countries with real cities (approximate
 * lon/lat), on top of the real-world border data in
 * assets/world_countries.geojson. Will grow toward the full 100+ country /
 * per-country-city vision once this slice is solid.
 */
object WorldData {

    val players = listOf(
        Player(id = "human", name = "You", isAi = false),
        Player(id = "ai_red", name = "Red AI", isAi = true)
    )

    /** IDs match the "id" field in the world_countries.geojson asset. */
    val countryIds = listOf("BRA", "AUS", "CHN")

    fun initialCities(): Map<String, City> {
        val human = players[0].id
        val ai = players[1].id

        val list = listOf(
            City("br_sp", "Sao Paulo", "BRA", -46.63, -23.55, listOf("br_rio", "br_bsb"), human, 5),
            City("br_rio", "Rio de Janeiro", "BRA", -43.20, -22.90, listOf("br_sp", "br_bsb"), human, 3),
            City("br_bsb", "Brasilia", "BRA", -47.93, -15.78, listOf("br_sp", "br_rio"), human, 3),

            City("au_syd", "Sydney", "AUS", 151.21, -33.87, listOf("au_mel", "au_per"), human, 3),
            City("au_mel", "Melbourne", "AUS", 144.96, -37.81, listOf("au_syd", "au_per"), human, 2),
            City("au_per", "Perth", "AUS", 115.86, -31.95, listOf("au_syd", "au_mel"), human, 2),

            City("cn_bj", "Beijing", "CHN", 116.41, 39.90, listOf("cn_sh", "cn_gz"), ai, 6),
            City("cn_sh", "Shanghai", "CHN", 121.47, 31.23, listOf("cn_bj", "cn_gz"), ai, 3),
            City("cn_gz", "Guangzhou", "CHN", 113.26, 23.13, listOf("cn_bj", "cn_sh"), ai, 3)
        )
        return list.associateBy { it.id }
    }
}
