plugins {
    alias(libs.plugins.android.library)
}

android {
    defaultConfig {
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()
        ndkVersion = libs.versions.ndkVersion.get()
    }

    externalNativeBuild {
        ndkBuild {
            path = File("src/main/jni/Android.mk")
        }
    }
    lint {
        abortOnError = true
        warningsAsErrors = true
    }
    namespace = "de.saschahlusiak.freebloks.server"
}

dependencies {
    api(project(":game"))
}