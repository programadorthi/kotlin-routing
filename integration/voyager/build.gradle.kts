plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    alias(libs.plugins.jetbrains.compose)
    id("org.jlleitschuh.gradle.ktlint")
    id("org.jetbrains.kotlinx.kover")
    alias(libs.plugins.maven.publish)
}

configureCommon()
configureJvm()
setupJvmToolchain()

kotlin {
    explicitApi()

    setCompilationOptions()
    configureSourceSets()

    js(IR) {
        nodejs()
        browser()
    }

    configureJs()

    macosX64()
    macosArm64()
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain {
            dependencies {
                api(projects.resources)
                api(libs.voyager.navigator)
                implementation(compose.runtime)
                implementation(compose.runtimeSaveable)
            }
        }

        val jvmMain by getting {
            dependsOn(commonMain.get())
            dependencies {
                api(libs.slf4j.api)
            }
        }
        val jvmTest by getting {
            dependsOn(commonTest.get())
            dependencies {
                implementation(libs.test.junit)
                implementation(libs.test.coroutines.debug)
                implementation(libs.test.kotlin.test.junit)
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
