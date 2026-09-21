package com.conquestofnations.app.map

import androidx.compose.ui.graphics.Color

/**
 * Placeholder territory model.
 * Real data will be generated from world geodata (country -> list of cities
 * with lat/lon, adjacency, and rendering polygons) rather than hand-coded.
 */
data class Territory(
    val id: String,
    val name: String,
    val ownerColor: Color,
    val centerXFraction: Float, // 0f..1f position on the map canvas
    val centerYFraction: Float,
    val garrisonCount: Int
)

fun sampleTerritories(): List<Territory> = listOf(
    Territory("usa", "United States", Color(0xFF3B6EA5), 0.18f, 0.35f, 6),
    Territory("bra", "Brazil", Color(0xFF4C9F5A), 0.28f, 0.65f, 3),
    Territory("rus", "Russia", Color(0xFFB33B3B), 0.65f, 0.22f, 10),
    Territory("chn", "China", Color(0xFFC9A227), 0.75f, 0.38f, 8),
    Territory("aus", "Australia", Color(0xFF7A4FA3), 0.85f, 0.78f, 2)
)
