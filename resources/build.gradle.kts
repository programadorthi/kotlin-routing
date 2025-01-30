plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    alias(libs.plugins.ksp)
    id("org.jetbrains.kotlinx.kover")
    alias(libs.plugins.maven.publish)
}

applyBasicSetup()

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(projects.core)
                api(libs.ktor.resources)
                api(libs.serialization.core)
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
    arg("Routing_Module_Name", "Resources")
}
