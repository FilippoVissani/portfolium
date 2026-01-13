plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.kover)
    alias(libs.plugins.versions)
    alias(libs.plugins.graalvm)
    application
}

group = "io.github.filippovissani"
version = "5.0.5"

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

tasks.jar {
    manifest {
        attributes["Main-Class"] = "io.github.filippovissani.portfolium.MainKt"
    }
    // Include dependencies in the JAR
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
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

// ============================================
// GraalVM Native Image Configuration
// ============================================
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

graalvmNative {
    binaries {
        named("main") {
            javaLauncher.set(
                javaToolchains.launcherFor {
                    languageVersion.set(JavaLanguageVersion.of(25))
                },
            )
            imageName.set("portfolium")
            mainClass.set("io.github.filippovissani.portfolium.MainKt")

            buildArgs.add("--verbose")
            buildArgs.add("--no-fallback")
            buildArgs.add("-H:+ReportExceptionStackTraces")

            // Enable all-public reflection for SnakeYAML and other libraries
            buildArgs.add("-H:+AddAllCharsets")
            buildArgs.add("-H:+IncludeAllLocales")

            // Resource inclusion
            buildArgs.add("-H:IncludeResources=.*\\.properties")
            buildArgs.add("-H:IncludeResources=.*\\.xml")
            buildArgs.add("-H:IncludeResources=static/.*")

            // Enable HTTP
            buildArgs.add("--enable-url-protocols=http,https")

            // Fix for Netty and logging issues with Java 25
            buildArgs.add("--initialize-at-run-time=io.netty")
            buildArgs.add("--initialize-at-run-time=ch.qos.logback")
            buildArgs.add("--initialize-at-run-time=org.slf4j")

            // Memory settings
            buildArgs.add("-J-Xmx4g")
        }
    }

    // Use agent mode for automatic metadata generation
    agent {
        defaultMode.set("standard")
        modes {
            standard {
            }
        }
        metadataCopy {
            inputTaskNames.add("run")
            outputDirectories.add("src/main/resources/META-INF/native-image")
            mergeWithExisting.set(true)
        }
    }
}
