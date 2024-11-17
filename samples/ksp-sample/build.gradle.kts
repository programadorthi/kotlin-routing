import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    kotlin("multiplatform")
    alias(libs.plugins.ksp)
    alias(libs.plugins.routing)
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
                implementation(libs.routing.core)
                implementation(libs.routing.compose)
                implementation(libs.routing.voyager)
                implementation(libs.routing.annotations)
                implementation(compose.runtime)
            }
        }
    }
}

/*dependencies {
    add("kspJvm", projects.ksp.coreProcessor)
}

configurations.all {
    resolutionStrategy.dependencySubstitution {
        substitute(module("dev.programadorthi.routing:core"))
            .using(project(":core"))
        substitute(module("dev.programadorthi.routing:compose"))
            .using(project(":integration:compose"))
        substitute(module("dev.programadorthi.routing:voyager"))
            .using(project(":integration:voyager"))
        substitute(module("dev.programadorthi.routing:ksp-core-annotations"))
            .using(project(":ksp:core-annotations"))
    }
}*/
