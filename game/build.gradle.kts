plugins {
    id("com.android.library")
    id("kotlin-android")
}

android {
    defaultConfig {
        compileSdk = 36
        minSdk = 21
    }
    testOptions {
        unitTests {
            isReturnDefaultValues = true
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        jvmToolchain(17)
    }
    namespace = "de.saschahlusiak.freebloks.game"
}

dependencies {
    testImplementation("androidx.test.ext:junit:1.2.1")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")

    implementation(project(":common"))

    implementation("androidx.core:core-ktx:1.15.0")
}
