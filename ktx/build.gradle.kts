plugins {
    id("com.android.library")
    id("kotlin-android")
}

android {
    compileSdk = 35
    ndkVersion = "27.1.12297006"

    defaultConfig {
        minSdk = 21
    }

    externalNativeBuild {
        ndkBuild {
            path = File("src/main/jni/Android.mk")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        jvmToolchain(17)
    }
    namespace = "de.saschahlusiak.freebloks.ktx"
}

dependencies {

}