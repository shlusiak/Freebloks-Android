plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}

android {
    namespace = "de.saschahlusiak.freebloks.data"
    compileSdk = 36

    defaultConfig {
        minSdk = 23
    }
    room {
        schemaDirectory("$projectDir/schemas")
        generateKotlin = true
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

    implementation(libs.preference.ktx)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
}