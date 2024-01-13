plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("com.android.library")
    id("org.jlleitschuh.gradle.ktlint")
    id("org.jetbrains.kotlinx.kover")
    alias(libs.plugins.maven.publish)
}

applyBasicSetup()

configureAndroid()

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(projects.core)
                api(libs.serialization.json)
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(libs.androidx.activity)
                implementation(libs.androidx.startup)
            }
        }
    }
}

android {
    namespace = "dev.programadorthi.routing.core.stack"
}
