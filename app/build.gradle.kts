plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.google.services)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.compose)
}

android {
    defaultConfig {
        compileSdk = 36
        minSdk = 23
        targetSdk = 36
        ndkVersion = "28.2.13676358"

        versionCode = 172
        versionName = "1.7.2"

        @Suppress("UnstableApiUsage")
        androidResources.localeFilters += setOf("en", "de", "es", "fr", "ja", "pt", "ro", "ru", "zh-rHK")
    }
    flavorDimensions += listOf("app", "store")
    productFlavors {
        create("standard") {
            dimension = "app"
            applicationId = "de.saschahlusiak.freebloks"
        }
        create("vip") {
            dimension = "app"
            applicationId = "de.saschahlusiak.freebloksvip"
        }
        create("google") {
            dimension = "store"
            buildConfigField("String", "APP_STORE_NAME", "\"Google Play Store\"")
            buildConfigField("String", "APP_STORE_LINK", "\"https://play.google.com/store/apps/details?id=%s\"")
        }
//        create("amazon") {
//            dimension = "store"
//            buildConfigField("String", "APP_STORE_NAME", "\"Amazon App Store\"")
//            buildConfigField("String", "APP_STORE_LINK", "\"http://www.amazon.com/gp/mas/dl/android?p=%s\"")
//        }
        create("fdroid") {
            dimension = "store"
            buildConfigField("String", "APP_STORE_NAME", "\"F-Droid\"")
            buildConfigField("String", "APP_STORE_LINK", "\"https://f-droid.org/en/packages/%s\"")
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    testOptions {
        unitTests {
            isReturnDefaultValues = true
            isIncludeAndroidResources = true
        }
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    lint {
        abortOnError = true
        warningsAsErrors = true
        baseline = file("lint-baseline.xml")
    }
    namespace = "de.saschahlusiak.freebloks"
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xannotation-default-target=param-property")
    }
}

val googleImplementation by configurations
//val amazonImplementation by configurations

dependencies {
    testImplementation(libs.junit)
    testImplementation(libs.mockito.core)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.opengl.api)
    testImplementation(libs.robolectric)
    testImplementation(libs.espresso.core)
    testImplementation(libs.junit.ext)

    implementation(project(":common"))
    implementation(project(":themes"))
    implementation(project(":game"))
    implementation(project(":server"))
    implementation(project(":ktx"))
    implementation(project(":data"))

    implementation(libs.fragment.ktx)
    implementation(libs.core.ktx)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    implementation(libs.material)

    implementation(platform(libs.compose.bom))
    implementation(libs.activity.compose)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons)
    implementation(libs.compose.animation)
    implementation(libs.coil.compose)
    implementation(libs.compose.ui.tooling)

    implementation(libs.preference.ktx)

    /// NON-FREE DEPENDENCIES, only included in "google" store flavor, excluded in "fdroid"
    googleImplementation(project(":google-services"))
}
