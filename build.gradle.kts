buildscript {
    repositories {
        mavenCentral()
        google()
    }

    dependencies {
        // https://developer.android.com/studio/releases/gradle-plugin
        classpath ("com.android.tools.build:gradle:9.0.0")

        // Kotlin
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.2.20")
        classpath("org.jetbrains.kotlin:compose-compiler-gradle-plugin:2.2.20")
        classpath("com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:2.3.2")

        // Hilt
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.59.1")

        // Room
        classpath("androidx.room:androidx.room.gradle.plugin:2.8.4")

        // https://developers.google.com/android/guides/releases
        classpath("com.google.gms:google-services:4.4.4")
        classpath("com.google.firebase:firebase-crashlytics-gradle:3.0.6")
    }
}

allprojects {
    repositories {
        mavenCentral()
        google()
    }
}
