plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "de.saschahlusiak.freebloks.data"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }
    room3 {
        schemaDirectory("$projectDir/schemas")
    }
    buildFeatures {
        compose = true
    }
    lint {
        abortOnError = true
        warningsAsErrors = true
    }
}

dependencies {
    implementation(project(":game"))

    implementation(libs.room.runtime)
    ksp(libs.room.compiler)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.material3)
    implementation(libs.compose.ui.tooling)

    implementation(libs.preference.ktx)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
}