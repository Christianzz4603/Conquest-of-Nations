package com.conquestofnations.app.game

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Core game state for the first playable slice: a handful of cities across
 * a few countries, one human player and one AI, turn-based movement and
 * simple deterministic combat (no unit types or dice yet -- those come
 * with the full military layer later).
 */
class GameState(
    val countries: Map<String, Country>,
    initialCities: Map<String, City>,
    val players: List<Player>
) {
    val cities = mutableStateMapOf<String, City>().apply { putAll(initialCities) }

    var currentPlayerId by mutableStateOf(players.first().id)
        private set

    fun cityOwner(cityId: String): String? = cities[cityId]?.ownerId

    /**
     * Move (or attack) a garrison from one city to an adjacent city.
     * Leaves 1 unit behind at the source as a garrison.
     * Returns a short result message for the UI.
     */
    fun moveOrAttack(fromCityId: String, toCityId: String): String {
        val from = cities[fromCityId] ?: return "Unknown source city"
        val to = cities[toCityId] ?: return "Unknown target city"

        if (toCityId !in from.adjacentCityIds) return "$'{'to.name'}' is not adjacent to $'{'from.name'}'"
        if (from.ownerId != currentPlayerId) return "You don't control $'{'from.name'}'"
        if (from.garrison <= 1) return "Not enough troops to move from $'{'from.name'}'"

        val movingTroops = from.garrison - 1
        cities[fromCityId] = from.copy(garrison = 1)

        return if (to.ownerId == from.ownerId) {
            cities[toCityId] = to.copy(garrison = to.garrison + movingTroops)
            "Reinforced $'{'to.name'}' (+$'{'movingTroops'}')"
        } else {
            if (movingTroops > to.garrison) {
                val remaining = movingTroops - to.garrison
                cities[toCityId] = to.copy(ownerId = from.ownerId, garrison = remaining)
                "Captured $'{'to.name'}'!"
            } else {
                cities[toCityId] = to.copy(garrison = to.garrison - movingTroops)
                "Attack on $'{'to.name'}' repelled"
            }
        }
    }

    /** Ends the current player's turn and gives every city +1 garrison. */
    fun endTurn() {
        val currentIndex = players.indexOfFirst { it.id == currentPlayerId }
        val nextIndex = (currentIndex + 1) % players.size
        currentPlayerId = players[nextIndex].id
        cities.keys.toList().forEach { id ->
            val city = cities.getValue(id)
            cities[id] = city.copy(garrison = city.garrison + 1)
        }
    }
}
