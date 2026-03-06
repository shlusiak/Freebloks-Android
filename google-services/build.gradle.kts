plugins {
    id("com.android.library")
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
