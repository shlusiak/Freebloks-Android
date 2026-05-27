plugins {
    alias(libs.plugins.android.library)
}

android {
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
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

    implementation(libs.firebase.crashlytics)
}
