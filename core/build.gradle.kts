import kotlinx.kover.gradle.plugin.dsl.KoverProjectExtension

plugins {
    kotlin("multiplatform")
    alias(libs.plugins.ksp)
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
        commonTest {
            dependencies {
                implementation(projects.ksp.coreAnnotations)
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

dependencies {
    add("kspJvmTest", projects.ksp.coreProcessor)
}

ksp {
    arg("Routing_Module_Name", "Core")
}
