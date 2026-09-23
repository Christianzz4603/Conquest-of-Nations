plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.conquestofnations.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.conquestofnations.app"
        minSdk = 30
        targetSdk = 34
        versionCode = 1
        versionName = "0.1.0"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
}

/**
 * Downloads the full real-world country borders (all ~180 countries) from the
 * public-domain johan/world.geo.json dataset at build time, straight into
 * assets. This keeps the data complete and exactly as published upstream --
 * no hand-transcribed subset -- and always in sync with the source.
 */
val worldGeoJsonAsset = file("src/main/assets/world_countries.geojson")

tasks.register("downloadWorldGeoJson") {
    outputs.file(worldGeoJsonAsset)
    doLast {
        worldGeoJsonAsset.parentFile.mkdirs()
        val url = "https://raw.githubusercontent.com/johan/world.geo.json/master/countries.geo.json"
        ant.withGroovyBuilder {
            "get"("src" to url, "dest" to worldGeoJsonAsset, "skipexisting" to false)
        }
    }
}

tasks.named("preBuild") {
    dependsOn("downloadWorldGeoJson")
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("com.google.android.gms:play-services-nearby:19.3.0")
}
