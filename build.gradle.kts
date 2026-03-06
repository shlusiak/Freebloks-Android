buildscript {
    repositories {
        mavenCentral()
        google()
    }

    dependencies {
        // https://developer.android.com/studio/releases/gradle-plugin
        classpath(libs.gradle)

        // Kotlin
        classpath(libs.kotlin.gradle.plugin)
        classpath(libs.compose.compiler.gradle.plugin)
        classpath(libs.com.google.devtools.ksp.gradle.plugin)

        // Hilt
        classpath(libs.hilt.android.gradle.plugin)

        // Room
        classpath(libs.androidx.room.gradle.plugin)

        // https://developers.google.com/android/guides/releases
        classpath(libs.google.services)
        classpath(libs.firebase.crashlytics.gradle)
    }
}

allprojects {
    repositories {
        mavenCentral()
        google()
    }
}
