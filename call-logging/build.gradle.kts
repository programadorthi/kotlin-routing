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
                api(projects.core)
            }
        }
    }
}

configure<KoverReportExtension> {
    filters {
        excludes {
            packages("dev.programadorthi.routing.callloging")
        }
    }
}
