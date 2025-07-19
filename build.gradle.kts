buildscript {
    repositories {
        mavenCentral()
        google()
    }

    dependencies {
        // https://developer.android.com/studio/releases/gradle-plugin
        classpath ("com.android.tools.build:gradle:8.11.1")

        // Kotlin
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.2.0")
        classpath("org.jetbrains.kotlin:compose-compiler-gradle-plugin:2.2.0")
        classpath("com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:2.2.0-2.0.2")

        // Hilt
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.57")

        // Room
        classpath("androidx.room:androidx.room.gradle.plugin:2.7.2")

        // https://developers.google.com/android/guides/releases
        classpath("com.google.gms:google-services:4.4.3")
        classpath("com.google.firebase:firebase-crashlytics-gradle:3.0.4")
    }
}

allprojects {
    repositories {
        mavenCentral()
        google()
    }
}