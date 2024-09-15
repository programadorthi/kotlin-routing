plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.compose.compiler)
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
                implementation(compose.runtimeSaveable)
                implementation(libs.serialization.json)
            }
        }
    }
}
