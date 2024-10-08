import kotlinx.kover.gradle.plugin.dsl.KoverProjectExtension

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.kotlinx.kover")
    alias(libs.plugins.maven.publish)
}

applyBasicSetup()

darwinTargetsFramework {
    export(libs.ktor.utils)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(libs.ktor.events)
                api(libs.ktor.http)
            }
        }
    }
}

configure<KoverProjectExtension> {
    reports {
        filters {
            excludes {
                packages(
                    "dev.programadorthi.routing.core.application",
                    "dev.programadorthi.routing.core.errors",
                    "dev.programadorthi.routing.core.logging",
                )
            }
        }
    }
}
