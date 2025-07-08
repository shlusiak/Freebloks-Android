plugins {
    id("com.android.library")
    id("kotlin-android")
}

android {
    compileSdk = 36

    defaultConfig {
        minSdk = 21
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        jvmToolchain(17)
    }
    namespace = "de.saschahlusiak.freebloks.googleServices"
}

dependencies {
    implementation(project(":common"))

    implementation("androidx.core:core-ktx:1.17.0-alpha01")

    // For Google+ integration
    // https://developers.google.com/android/guides/releases
    implementation("com.google.android.gms:play-services-games:23.2.0")
    implementation("com.google.android.gms:play-services-auth:21.3.0")

    // https://firebase.google.com/support/release-notes/android
    implementation("com.google.firebase:firebase-analytics:22.4.0")
    implementation("com.google.firebase:firebase-crashlytics:19.4.4")
}
