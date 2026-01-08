import org.gradle.api.artifacts.VersionCatalogsExtension

plugins {
    alias(libs.plugins.kotlin.jvm)
    jacoco
    application
}

group = "io.github.filippovissani"
version = "3.4.0"

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
    jvmToolchain(25)
}

application {
    mainClass.set("io.github.filippovissani.portfolium.MainKt")
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport) // Generate coverage report after tests
}

jacoco {
    toolVersion = libs.findVersion("jacoco").get().toString()
}

tasks.jacocoTestReport {
    dependsOn(tasks.test) // Tests are required to run before generating the report
    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = "0.0".toBigDecimal() // Set minimum coverage threshold
            }
        }
    }
}

tasks.check {
    dependsOn(tasks.jacocoTestCoverageVerification)
}