import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    kotlin("multiplatform")
    alias(libs.plugins.ksp)
    //id("dev.programadorthi.routing") version "0.0.99"
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.compose.compiler)
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
                implementation(projects.integration.compose)
                implementation(projects.integration.voyager)
                implementation(projects.ksp.coreAnnotations)
                implementation(compose.runtime)
            }
        }
    }
}

dependencies {
    add("kspJvm", projects.ksp.coreProcessor)
}

/*configurations.all {
    resolutionStrategy.dependencySubstitution {
        substitute(module("dev.programadorthi.routing:core"))
            .using(project(":core"))
            .because("KSP gradle plugin have maven central dependencies")
    }
}*/
