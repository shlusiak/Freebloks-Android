plugins {
    id("com.android.library")
    id("kotlin-android")
}

android {
    defaultConfig {
        compileSdk = 36
        minSdk = 21
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

    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.fragment:fragment-ktx:1.8.6")

    api("androidx.compose.runtime:runtime-android:1.7.8")
}
