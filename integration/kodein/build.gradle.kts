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
                implementation(projects.core)
                implementation(libs.kodein.di)
            }
        }

        commonTest {
            dependencies {
                implementation(projects.statusPages)
            }
        }
    }
}

configure<KoverReportExtension> {
    filters {
        excludes {
            packages(
                "dev.programadorthi.routing.kodein.ext",
            )
        }
    }
}
