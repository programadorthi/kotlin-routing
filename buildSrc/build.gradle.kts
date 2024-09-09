plugins {
    `kotlin-dsl`
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(libs.plugin.kotlin)
    implementation(libs.plugin.kover)
    // FIXME: Kotlin and AGP plugins need to be loaded in the same place
    implementation(libs.plugin.android)
    implementation(libs.plugin.kotlinter)
    implementation(libs.tomlj)
}
