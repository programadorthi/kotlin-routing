plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ksp)
    id("org.jetbrains.kotlinx.kover")
    alias(libs.plugins.maven.publish)
}

applyBasicSetup()

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(projects.resources)
                implementation(compose.runtime)
                implementation(compose.runtimeSaveable)
                implementation(libs.serialization.json)
            }
        }
        commonTest {
            dependencies {
                implementation(projects.ksp.coreAnnotations)
            }
        }
    }
}

dependencies {
    add("kspJvmTest", projects.ksp.coreProcessor)
}

ksp {
    arg("Routing_Module_Name", "Compose")
}
