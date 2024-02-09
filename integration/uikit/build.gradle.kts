plugins {
    kotlin("multiplatform")
    id("org.jlleitschuh.gradle.ktlint")
    id("org.jetbrains.kotlinx.kover")
    alias(libs.plugins.maven.publish)
}

applyBasicSetup()

darwinTargetsFramework(
    targets = {
        listOf(iosArm64(), iosSimulatorArm64(), iosX64())
    },
    action = {
        baseName = "UIKitShared"
        export(libs.ktor.utils)
        export(projects.core)
    },
)

kotlin {
    sourceSets {
        darwinMain {
            dependencies {
                api(projects.core)
            }
        }

        darwinTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}
