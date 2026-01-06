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
    testImplementation(libs.findLibrary("kotlinTest").get())
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