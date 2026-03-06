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

    implementation(libs.javax.inject)
    implementation(libs.core.ktx)

    implementation(libs.play.services.games)
    implementation(libs.play.services.auth)

    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
}
