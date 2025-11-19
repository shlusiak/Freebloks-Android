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
    testImplementation("androidx.test.ext:junit:1.3.0")

    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.fragment:fragment-ktx:1.8.9")

    api("androidx.compose.runtime:runtime-android:1.9.4")
}
