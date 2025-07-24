plugins {
    id("com.android.library")
    id("kotlin-android")
}

android.buildFeatures.buildConfig = true

android {
    compileSdk = 36

    defaultConfig {
        minSdk = 23
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        jvmToolchain(17)
    }
    namespace = "de.saschahlusiak.freebloks.theme"
}

dependencies {
    implementation("androidx.core:core-ktx:1.17.0-beta01")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")

    testImplementation("junit:junit:4.13.2")
    testImplementation("androidx.test.ext:junit:1.2.1")
    testImplementation("org.robolectric:robolectric:4.15.1")
}
