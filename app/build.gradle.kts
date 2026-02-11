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
        minSdk = 23
        targetSdk = 36
        ndkVersion = "28.2.13676358"

        versionCode = 171
        versionName = "1.7.1"

        @Suppress("UnstableApiUsage")
        androidResources.localeFilters += setOf("en", "de", "es", "fr", "ja", "pt", "ro", "ru", "zh-rHK")
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        jvmToolchain(17)
    }
    kotlin {
        compilerOptions {
            freeCompilerArgs.add("-Xannotation-default-target=param-property")
        }
    }
    namespace = "de.saschahlusiak.freebloks"
}

val googleImplementation by configurations
//val amazonImplementation by configurations

dependencies {
    // for unit tests
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.21.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
    testImplementation("org.khronos:opengl-api:gl1.1-android-2.1_r1")
    testImplementation("org.robolectric:robolectric:4.16")
    testImplementation("androidx.test.espresso:espresso-core:3.7.0")
    testImplementation("androidx.test.ext:junit:1.3.0")

    implementation(project(":common"))
    implementation(project(":themes"))
    implementation(project(":game"))
    implementation(project(":server"))
    implementation(project(":ktx"))
    implementation(project(":data"))

    implementation("androidx.fragment:fragment-ktx:1.8.9")
    implementation("androidx.core:core-ktx:1.17.0")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.59.1")
    ksp("com.google.dagger:hilt-compiler:2.59.1")
    testImplementation("com.google.dagger:hilt-android-testing:2.57.2")
    kspTest("com.google.dagger:hilt-android-compiler:2.57.2")

    // https://github.com/material-components/material-components-android
    // https://mvnrepository.com/artifact/com.google.android.material/material
    implementation("com.google.android.material:material:1.13.0")

    // Compose
    implementation(platform("androidx.compose:compose-bom:2026.01.01"))
    implementation("androidx.activity:activity-compose:1.12.3")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended-android:1.7.8")
    implementation("androidx.compose.animation:animation")
    implementation("io.coil-kt:coil-compose:2.7.0")

    compileOnly("androidx.compose.ui:ui-tooling")

    // https://mvnrepository.com/artifact/androidx.preference/preference
    implementation("androidx.preference:preference-ktx:1.2.1")

    /// NON-FREE DEPENDENCIES, only included in "google" and "amazon" store flavours, excluded in "fdroid"
    /// =====================

    googleImplementation(project(":google-services"))
//    amazonImplementation(project(":google-services"))
}
