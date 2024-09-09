import kotlinx.kover.gradle.plugin.dsl.KoverProjectExtension

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.kotlinx.kover")
    alias(libs.plugins.maven.publish)
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

configure<KoverProjectExtension> {
    reports {
        filters {
            excludes {
                packages("dev.programadorthi.routing.callloging")
            }
        }
    }
}
