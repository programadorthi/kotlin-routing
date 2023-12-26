import kotlinx.kover.gradle.plugin.dsl.KoverReportExtension

plugins {
    kotlin("multiplatform")
    id("org.jlleitschuh.gradle.ktlint")
    id("org.jetbrains.kotlinx.kover")
    alias(libs.plugins.maven.publish)
}

applyBasicSetup()

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

configure<KoverReportExtension> {
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
