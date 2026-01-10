plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.kover)
    alias(libs.plugins.versions)
    alias(libs.plugins.dependencyCheck)
    alias(libs.plugins.diktat)
    application
}

group = "io.github.filippovissani"
version = "5.0.2"

repositories {
    mavenCentral()
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

dependencies {
    implementation(libs.findLibrary("kotlinCsv").get())
    implementation(libs.findLibrary("snakeyaml").get())
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
    // Konsist for architecture testing
    testImplementation(libs.findLibrary("konsist").get())
}

kotlin {
    jvmToolchain(25)
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
        exclude("**/WebView.kt")
        exclude { it.file.path.contains("/view/") }
    }
}

application {
    mainClass.set("io.github.filippovissani.portfolium.MainKt")
}

tasks.test {
    useJUnitPlatform()
}

tasks.check {
    dependsOn(tasks.named("ktlintCheck"))
    dependsOn(tasks.named("koverVerify"))
}

// ============================================
// Code Quality Plugin Configurations
// ============================================

// Kover - Code Coverage
kover {
    reports {
        total {
            html {
                onCheck = true
            }
            xml {
                onCheck = true
            }
            verify {
                onCheck = true
                rule {
                    minBound(0) // Minimum coverage threshold (0% to start)
                }
            }
        }
    }
}

// Gradle Versions Plugin - Dependency Updates
tasks.named("dependencyUpdates").configure {
    this as com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
    rejectVersionIf {
        isNonStable(candidate.version) && !isNonStable(currentVersion)
    }
    checkForGradleUpdate = true
    outputFormatter = "html"
    outputDir = "build/reports/dependencyUpdates"
    reportfileName = "report"
}

fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return isStable.not()
}

// OWASP Dependency Check - Security Scanning
dependencyCheck {
    formats = listOf("HTML", "JSON", "XML")
    scanConfigurations = listOf("runtimeClasspath")
    suppressionFile = "$projectDir/config/owasp/suppressions.xml"
    failBuildOnCVSS = 7.0f // Fail build if CVSS score >= 7
    analyzers.assemblyEnabled = false
    analyzers.nuspecEnabled = false
}

// Diktat - Strict Kotlin Style Checker
diktat {
    inputs {
        include("src/**/*.kt")
        exclude("**/build/**")
    }
    debug = false
    ignoreFailures = true // Set to false to enforce strict rules
}
