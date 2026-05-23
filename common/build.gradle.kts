plugins {
    alias(libs.plugins.android.library)
}

android {
    defaultConfig {
        compileSdk = 37
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

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui.tooling)
    api(libs.compose.runtime)
}
