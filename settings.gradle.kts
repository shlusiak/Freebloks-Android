rootProject.name = "Freebloks-Android"

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

include(":app")
include(":themes")
include(":common")
include(":game")
include(":google-services")
include(":server")
include(":ktx")
include(":data")
