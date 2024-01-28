plugins {
    alias(libs.plugins.android.library)
    id("org.jlleitschuh.gradle.ktlint")
    id("org.jetbrains.kotlinx.kover")
    alias(libs.plugins.maven.publish)
}

android {
    namespace = "dev.programadorthi.routing.android"
    compileSdk = 34

    defaultConfig {
        minSdk = 23
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    api(projects.resources)
}