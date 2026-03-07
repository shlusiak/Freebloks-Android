plugins {
    alias(libs.plugins.android.library)
}

android {
    defaultConfig {
        compileSdk = 36
        minSdk = 23
    }
    testOptions {
        unitTests {
            isReturnDefaultValues = true
        }
    }
    lint {
        abortOnError = true
        warningsAsErrors = true
    }
    namespace = "de.saschahlusiak.freebloks.game"
}

dependencies {
    testImplementation(libs.junit.ext)
    testImplementation(libs.kotlinx.coroutines.test)

    implementation(project(":common"))

    implementation(libs.core.ktx)
}
