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
                implementation(projects.kspAnnotations)
                implementation(libs.ksp.api)
            }
        }
    }
}
