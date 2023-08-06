plugins {
    kotlin("multiplatform")
    id("org.jlleitschuh.gradle.ktlint")
    id("org.jetbrains.kotlinx.kover")
    alias(libs.plugins.atomicfu)
    alias(libs.plugins.maven.publish)
}

applyBasicSetup()

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(projects.sessions)
                api(libs.atomicfu)
            }
        }
        commonTest {
            dependencies {
                implementation(projects.statusPages)
            }
        }
    }
}
