plugins {
    id("com.android.library")
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
    namespace = "de.saschahlusiak.freebloks.common"
}

dependencies {
    testImplementation(libs.junit.ext)

    implementation(libs.core.ktx)
    implementation(libs.fragment.ktx)

    api(libs.compose.runtime)
}
