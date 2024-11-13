plugins {
    kotlin("multiplatform")
    id("org.jetbrains.kotlinx.kover")
    alias(libs.plugins.maven.publish)
}

applyBasicSetup()
