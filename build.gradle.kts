buildscript {
    repositories {
        mavenCentral()
        google()
    }

    dependencies {
        // https://developer.android.com/studio/releases/gradle-plugin
        classpath ("com.android.tools.build:gradle:8.9.2")

        // Kotlin
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.1.20")
        classpath("org.jetbrains.kotlin:compose-compiler-gradle-plugin:2.1.20")
        classpath("com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:2.1.20-2.0.0")

        // Hilt
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.56.2")

        // Room
        classpath("androidx.room:androidx.room.gradle.plugin:2.7.1")

        // https://developers.google.com/android/guides/releases
        classpath("com.google.gms:google-services:4.4.2")
        classpath("com.google.firebase:firebase-crashlytics-gradle:3.0.3")
    }
}

allprojects {
    repositories {
        mavenCentral()
        google()
    }
}