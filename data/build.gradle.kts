plugins {
    id("com.android.library")
    id("kotlin-android")
    id("dagger.hilt.android.plugin")
    id("com.google.devtools.ksp")
    id("androidx.room")
}

android {
    namespace = "de.saschahlusiak.freebloks.data"
    compileSdk = 36

    defaultConfig {
        minSdk = 21
    }
    room {
        schemaDirectory("$projectDir/schemas")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    ksp {
        arg("room.generateKotlin", "true")
    }
}

dependencies {
    implementation(project(":game"))

    // Room for data persisting
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // https://mvnrepository.com/artifact/androidx.preference/preference
    implementation("androidx.preference:preference-ktx:1.2.1")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.53")
    ksp("com.google.dagger:hilt-compiler:2.53")
}