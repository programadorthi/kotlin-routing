import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.compose.compiler)
    id("org.jetbrains.kotlinx.kover")
    alias(libs.plugins.maven.publish)
}

configureCommon()
configureJvm()
setupJvmTarget()

kotlin {
    explicitApi()
    jvmToolchain(11)

    setCompilationOptions()
    configureSourceSets()

    js(IR) {
        nodejs()
        browser()
    }

    configureJs()

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        nodejs()
        browser()
    }

    configureWasm()

    macosX64()
    macosArm64()
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain {
            dependencies {
                api(projects.integration.compose)
                implementation(compose.runtime)
                implementation(compose.animation)
            }
        }

        jvmMain {
            dependencies {
                api(libs.slf4j.api)
            }
        }

        jvmTest {
            dependencies {
                implementation(libs.test.junit)
                implementation(libs.test.coroutines.debug)
                implementation(libs.test.kotlin.test.junit)
                implementation(compose.desktop.uiTestJUnit4)
                implementation(compose.desktop.currentOs)
            }
        }

        val nativeMain by creating {
            dependsOn(commonMain.get())
        }

        val macosMain by creating {
            dependsOn(nativeMain)
        }
        val macosX64Main by getting {
            dependsOn(macosMain)
        }
        val macosArm64Main by getting {
            dependsOn(macosMain)
        }
        val iosX64Main by getting {
            dependsOn(nativeMain)
        }
        val iosArm64Main by getting {
            dependsOn(nativeMain)
        }
    }
}
