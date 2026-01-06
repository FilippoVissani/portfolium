plugins {
    kotlin("jvm") version "2.2.21"
    application
}

group = "io.github.filippovissani"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.github.doyaaaaaken:kotlin-csv-jvm:1.9.3")
    testImplementation(kotlin("test"))
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