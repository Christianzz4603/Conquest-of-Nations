# Conquest of Nations

A real-world map conquest strategy game for Android.

- Conquer individual cities across 100+ countries
- Declare war, offer/accept alliances, trade resources
- Unit roster: soldiers, tanks, snipers, APCs, jets, warships
- Single player vs AI, fully offline
- Local multiplayer over WiFi/hotspot (Nearby Connections), no internet required

## Map data
The full real-world country border set (~180 countries, public domain) is
downloaded automatically at build time by the `downloadWorldGeoJson` Gradle
task (wired into `preBuild`) from the johan/world.geo.json dataset, straight
into `app/src/main/assets/world_countries.geojson`. That file isn't tracked
in git -- it's always fetched fresh, so it stays complete and accurate
rather than a hand-picked subset. Building requires internet access once
(Android Studio and the CI workflow both have it).

## Status
Compose UI + interactive map (tap-to-select, move/attack between adjacent
cities, end turn) over the full real-world border set. Currently playable
with cities in Brazil, Australia, and China; more countries/cities to come.

## Build
Open in Android Studio (Kotlin, min SDK 30) or run CI via GitHub Actions
(`.github/workflows/android-build.yml`).
