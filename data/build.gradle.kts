plugins {
    id("com.android.library")
    id("kotlin-android")
    id("dagger.hilt.android.plugin")
    id("com.google.devtools.ksp")
    id("androidx.room")
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        jvmToolchain(17)
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