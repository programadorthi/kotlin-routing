import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    kotlin("multiplatform")
    alias(libs.plugins.ksp)
    id("dev.programadorthi.routing") version "0.0.99"
}

kotlin {
    jvm {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        mainRun {
            mainClass.set("MainKt")
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.core)
                implementation(projects.ksp.coreAnnotations)
            }
        }
    }
}

configurations.all {
    resolutionStrategy.dependencySubstitution {
        substitute(module("dev.programadorthi.routing:core"))
            .using(project(":core"))
            .because("KSP gradle plugin have maven central dependencies")
    }
}
