plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    alias(libs.plugins.jetbrains.compose)
    id("org.jlleitschuh.gradle.ktlint")
    id("org.jetbrains.kotlinx.kover")
    alias(libs.plugins.maven.publish)
}

applyBasicSetup()

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(projects.resources)
                implementation(compose.runtime)
                implementation(libs.serialization.json)
            }
        }

        jvmMain {
            dependencies {
                implementation(compose.runtimeSaveable)
            }
        }
    }
}
