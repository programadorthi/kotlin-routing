plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("org.jlleitschuh.gradle.ktlint")
    id("org.jetbrains.kotlinx.kover")
    alias(libs.plugins.maven.publish)
}

applyBasicSetup()

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(projects.events)
                api(libs.ktor.resources)
                api(libs.serialization.core)
            }
        }
    }
}
