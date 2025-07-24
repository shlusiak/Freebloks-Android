plugins {
    id("com.android.library")
    id("kotlin-android")
}

android {
    defaultConfig {
        compileSdk = 36
        minSdk = 23
        consumerProguardFiles("consumer-rules.pro")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        jvmToolchain(17)
    }
    namespace = "de.saschahlusiak.freebloks.common"
}

dependencies {
    testImplementation("androidx.test.ext:junit:1.2.1")

    implementation("androidx.core:core-ktx:1.17.0-beta01")
    implementation("androidx.fragment:fragment-ktx:1.8.8")

    api("androidx.compose.runtime:runtime-android:1.8.3")
}
