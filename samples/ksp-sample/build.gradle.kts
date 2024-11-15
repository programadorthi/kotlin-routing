plugins {
    kotlin("jvm")
    alias(libs.plugins.ksp)
    //id("dev.programadorthi.routing") version "0.0.99"
}

dependencies {
    implementation(projects.core)
}

configurations.all {
    resolutionStrategy.dependencySubstitution {
        substitute(module("dev.programadorthi.routing:core"))
            .using(project(":core"))
            .because("KSP gradle plugin have maven central dependencies")
    }
}
