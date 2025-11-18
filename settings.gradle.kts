pluginManagement {
    val quarkusPluginVersion: String by settings
    val quarkusPluginId: String by settings
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
    plugins {
        id(quarkusPluginId) version quarkusPluginVersion
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "KotlinDemo"

include("quarkus-rest")
include("http4k-example")
