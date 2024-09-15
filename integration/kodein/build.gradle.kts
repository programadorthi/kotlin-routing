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

configure<KoverProjectExtension> {
    reports {
        filters {
            excludes {
                packages(
                    "dev.programadorthi.routing.kodein.ext",
                )
            }
        }
    }
}
