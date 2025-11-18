import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version "2.1.20"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.2"
    application
}

repositories {
    mavenCentral()
    mavenLocal()
}

val arrowVersion: String by project
val auth0Version: String by project
val http4kVersion: String by project
val jbcryptVersion: String by project
val kotestArrowVersion: String by project
val kotestVersion: String by project
val kotlinLoggingVersion: String by project
val logbackVersion: String by project
val mockkVersion: String by project
val restAssuredVersion: String by project
val slf4jVersion: String by project

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // Http4k
    implementation(platform("org.http4k:http4k-bom:$http4kVersion"))
    implementation("org.http4k:http4k-core")
    implementation("org.http4k:http4k-server-netty")
    implementation("org.http4k:http4k-format-jackson")
//    implementation("org.http4k:http4k-client-okhttp:$http4kVersion")
    implementation("org.http4k:http4k-config")
    implementation("org.http4k:http4k-api-openapi")
    implementation("org.http4k:http4k-api-ui-swagger")

    // Config
    implementation("com.sksamuel.hoplite:hoplite-core:2.9.0")
    runtimeOnly("com.sksamuel.hoplite:hoplite-yaml:2.9.0")

    // Logging
    implementation("io.github.microutils:kotlin-logging-jvm:$kotlinLoggingVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("org.slf4j:slf4j-api:$slf4jVersion")

    // Arrow-kt
    implementation("io.arrow-kt:arrow-core:$arrowVersion")
    implementation("io.arrow-kt:arrow-fx-coroutines:$arrowVersion")

    // BCrypt
    implementation("org.mindrot:jbcrypt:$jbcryptVersion")

    // JWT
    implementation("com.auth0:java-jwt:$auth0Version")

    // Test dependencies
    testImplementation("io.rest-assured:rest-assured:$restAssuredVersion")
    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
    testImplementation("io.kotest:kotest-property:$kotestVersion")
    testImplementation("io.kotest.extensions:kotest-assertions-arrow:$kotestArrowVersion")
    testImplementation("io.mockk:mockk:$mockkVersion")
}

group = "za.co.ee.learning"
version = "1.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions.jvmTarget.set(JvmTarget.JVM_21)
    compilerOptions.javaParameters.set(true)
}

tasks.test {
    useJUnitPlatform()
}

ktlint {
    version.set("1.5.0")
    verbose.set(true)
    android.set(false)
    outputToConsole.set(true)
    ignoreFailures.set(false)
    filter {
        exclude("**/generated/**")
        exclude("**/build/**")
    }
}
