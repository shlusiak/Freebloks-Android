plugins {
    id("com.android.application")
    id("kotlin-android")
    id("dagger.hilt.android.plugin")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    defaultConfig {
        compileSdk = 36
        minSdk = 21
        targetSdk = 36

        versionCode = 162
        versionName = "1.6.2"

        androidResources.localeFilters += setOf("en", "de", "es", "fr", "ja", "pt", "ro", "ru")
    }
    setFlavorDimensions(listOf("app", "store"))
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
        create("amazon") {
            dimension = "store"
            buildConfigField("String", "APP_STORE_NAME", "\"Amazon App Store\"")
            buildConfigField("String", "APP_STORE_LINK", "\"http://www.amazon.com/gp/mas/dl/android?p=%s\"")
        }
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
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
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
        abortOnError = false
        checkReleaseBuilds = false
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        jvmToolchain(17)
    }
    namespace = "de.saschahlusiak.freebloks"
}

val googleImplementation by configurations
val amazonImplementation by configurations

dependencies {
    // for unit tests
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.14.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    testImplementation("org.khronos:opengl-api:gl1.1-android-2.1_r1")
    testImplementation("org.robolectric:robolectric:4.14.1")
    testImplementation("androidx.test.espresso:espresso-core:3.6.1")
    testImplementation("androidx.test.ext:junit:1.2.1")

    implementation(project(":common"))
    implementation(project(":themes"))
    implementation(project(":game"))
    implementation(project(":server"))
    implementation(project(":ktx"))
    implementation(project(":data"))

    implementation("androidx.fragment:fragment-ktx:1.8.6")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.53")
    ksp("com.google.dagger:hilt-compiler:2.53")
    testImplementation("com.google.dagger:hilt-android-testing:2.53")
    kspTest("com.google.dagger:hilt-android-compiler:2.53")

    // https://github.com/material-components/material-components-android
    // https://mvnrepository.com/artifact/com.google.android.material/material
    implementation("com.google.android.material:material:1.12.0")

    // https://developer.android.com/jetpack/androidx/releases/lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")

    // Compose
    implementation(platform("androidx.compose:compose-bom:2025.03.00"))
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.animation:animation")
    implementation("androidx.compose.ui:ui-tooling")
    implementation("io.coil-kt:coil-compose:2.7.0")

    // https://mvnrepository.com/artifact/androidx.preference/preference
    implementation("androidx.preference:preference-ktx:1.2.1")

    /// NON-FREE DEPENDENCIES, only included in "google" and "amazon" store flavours, excluded in "fdroid"
    /// =====================

    googleImplementation(project(":google-services"))
    amazonImplementation(project(":google-services"))
}
