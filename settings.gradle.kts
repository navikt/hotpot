pluginManagement {
    includeBuild("build-logic")

    repositories {
        gradlePluginPortal()
        maven("https://navikt.github.io/hotlibs-gradle")
    }
}

plugins {
    id("no.nav.hjelpemidler.hotlibs") version "1.0"
}

rootProject.name = "hotpot"
include("app", "grunndata", "oebs")
