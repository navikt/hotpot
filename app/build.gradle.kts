plugins {
    id("buildlogic.kotlin-application-conventions")

    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(project(":oebs"))

    // hotlibs
    implementation(libs.hotlibs.core)
    implementation(libs.hotlibs.http)
    implementation(libs.hotlibs.logging)
    implementation(libs.hotlibs.serialization)
    implementation(libs.hotlibs.server)

    // GraphQL
    implementation(libs.graphql.ktor.server)

    // Ktor
    implementation(libs.ktor.client.resources)
    implementation(libs.ktor.server.di)
    implementation(libs.ktor.server.netty)
}

application {
    mainClass = "no.nav.hjelpemidler.hotspot.AppKt"
}
