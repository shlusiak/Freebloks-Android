rootProject.name = "Freebloks-Android"

include(":app")
include(":themes")
include(":common")
include(":game")
include(":google-services")
include(":server")
include(":ktx")
include(":data")

buildscript {
    repositories {
        mavenCentral()
        google()
    }

//    dependencies {
//        classpath("com.android.tools.build:gradle:8.13.1")
//    }
}

//plugins {
//    id("com.autonomousapps.build-health") version "3.4.1"
//}