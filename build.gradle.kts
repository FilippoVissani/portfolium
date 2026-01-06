import org.gradle.api.artifacts.VersionCatalogsExtension

plugins {
    kotlin("jvm") version "2.2.21"
    application
}

group = "io.github.filippovissani"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

dependencies {
    implementation(libs.findLibrary("kotlinCsv").get())
    implementation(libs.findLibrary("ktor-server-core").get())
    implementation(libs.findLibrary("ktor-server-netty").get())
    implementation(libs.findLibrary("ktor-server-html").get())
    implementation(libs.findLibrary("ktor-serialization-gson").get())
    implementation(libs.findLibrary("ktor-server-content-negotiation").get())
    implementation(libs.findLibrary("logback").get())
    testImplementation(libs.findLibrary("kotlinTest").get())
    // Kotest
    testImplementation(libs.findLibrary("kotest-assertions-core").get())
    testImplementation(libs.findLibrary("kotest-runner-junit5").get())
}

kotlin {
    jvmToolchain(21)
}

application {
    mainClass.set("io.github.filippovissani.portfolium.MainKt")
}

tasks.test {
    useJUnitPlatform()
}