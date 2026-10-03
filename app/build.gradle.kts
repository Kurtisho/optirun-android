import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.serialization)
}

// ORS key: local.properties on your machine, ORS_API_KEY env var in CI

// API keys: local.properties on your machine, env vars in CI
val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
val orsApiKey: String =
    localProps.getProperty("ORS_API_KEY") ?: System.getenv("ORS_API_KEY") ?: ""
val mapsApiKey: String =
    localProps.getProperty("MAPS_API_KEY") ?: System.getenv("MAPS_API_KEY") ?: ""

android {
    namespace = "com.kurtis.optirun"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.kurtis.optirun"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "ORS_API_KEY", "\"$orsApiKey\"")
        manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey
    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.navigation.compose)

    // DI
    implementation(libs.hilt.android)
    add("ksp", libs.hilt.compiler)
    implementation(libs.hilt.nav.compose)

    // Networking
    implementation(libs.retrofit)
    implementation(libs.retrofit.serialization)
    implementation(libs.okhttp.logging)
    implementation(libs.serialization.json)

    // Local cache
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    add("ksp",libs.room.compiler)

    // Async + location
    implementation(libs.coroutines.android)
    implementation(libs.play.location)
    implementation(libs.maps.compose)

    // Tests
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.coroutines.test)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}

// Hilt bundles a Kotlin metadata reader that only supports up to Kotlin 2.3.
// Force the newer one so it can read Kotlin 2.4 classes.
configurations.configureEach {
    resolutionStrategy.eachDependency {
        if (requested.group == "org.jetbrains.kotlin" && requested.name == "kotlin-metadata-jvm") {
            useVersion("2.4.20")
        }
    }
}