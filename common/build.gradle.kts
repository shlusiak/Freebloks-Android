plugins {
    id("com.android.library")
    id("kotlin-android")
}

android {
    defaultConfig {
        compileSdk = 36
        minSdk = 23
    }
    lint {
        abortOnError = true
        warningsAsErrors = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        jvmToolchain(17)
    }
    namespace = "de.saschahlusiak.freebloks.common"
}

dependencies {
    testImplementation(libs.junit.ext)

    implementation(libs.core.ktx)
    implementation(libs.fragment.ktx)

    api(libs.compose.runtime)
}
