pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://navikt.github.io/hotlibs-gradle")
    }
}

plugins {
    id("no.nav.hjelpemidler.hotlibs") version "1.0"
}

rootProject.name = "build-logic"
