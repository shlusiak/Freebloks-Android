plugins {
    id("com.android.library")
    id("kotlin-android")
}

android {
    compileSdk = 36

    defaultConfig {
        minSdk = 23
    }
    lint {
        abortOnError = true
        baseline = file("lint-baseline.xml")
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

    implementation("javax.inject:javax.inject:1")
    implementation("androidx.core:core-ktx:1.17.0")

    // For Google+ integration
    // https://developers.google.com/android/guides/releases
    implementation("com.google.android.gms:play-services-games:24.0.0")
    implementation("com.google.android.gms:play-services-auth:21.4.0")

    // https://firebase.google.com/support/release-notes/android
    implementation("com.google.firebase:firebase-analytics:23.0.0")
    implementation("com.google.firebase:firebase-crashlytics:20.0.3")
}
