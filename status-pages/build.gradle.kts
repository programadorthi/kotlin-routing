plugins {
    kotlin("multiplatform")
    id("org.jlleitschuh.gradle.ktlint")
    id("maven-publish")
}

applyBasicSetup()

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(projects.core)
            }
        }
    }
}
