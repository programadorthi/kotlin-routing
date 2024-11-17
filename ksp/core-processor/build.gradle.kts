plugins {
    kotlin("multiplatform")
    id("org.jetbrains.kotlinx.kover")
    alias(libs.plugins.maven.publish)
}

applyBasicSetup()

kotlin {
    sourceSets {
        jvmMain {
            dependencies {
                implementation(projects.core)
                implementation(projects.ksp.coreAnnotations)
                implementation(libs.kotlin.poet)
                implementation(libs.kotlin.poet.ksp)
                implementation(libs.ksp.api)
            }
        }
    }
}
