plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "de.saschahlusiak.freebloks.data"
    compileSdk = 37

    defaultConfig {
        minSdk = 23
    }
    room {
        schemaDirectory("$projectDir/schemas")
        generateKotlin = true
    }
    buildFeatures {
        compose = true
    }
    lint {
        abortOnError = true
        warningsAsErrors = true
    }
    kotlin {
        compilerOptions {
            freeCompilerArgs.add("-Xannotation-default-target=param-property")
        }
    }
}

dependencies {
    implementation(project(":game"))

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.material3)
    implementation(libs.compose.ui.tooling)

    implementation(libs.preference.ktx)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
}