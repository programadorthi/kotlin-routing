plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("org.jlleitschuh.gradle.ktlint")
    id("maven-publish")
}

applyBasicSetup()

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(projects.core)
                api(libs.ktor.resources)
                api(libs.serialization.core)
            }
        }
    }
}
