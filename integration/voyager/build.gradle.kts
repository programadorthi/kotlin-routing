import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ksp)
    id("org.jetbrains.kotlinx.kover")
    alias(libs.plugins.maven.publish)
}

configureCommon()
configureJvm()
setupJvmTarget()

kotlin {
    explicitApi()

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
                api(projects.resources)
                api(libs.voyager.navigator)
                implementation(compose.runtime)
                implementation(compose.runtimeSaveable)
            }
        }
        commonTest {
            dependencies {
                implementation(projects.ksp.coreAnnotations)
            }
        }

        jsMain {
            dependencies {
                implementation(libs.serialization.json)
            }
        }

        jvmMain {
            dependsOn(commonMain.get())
            dependencies {
                api(libs.slf4j.api)
            }
        }
        jvmTest {
            dependsOn(commonTest.get())
            dependencies {
                implementation(libs.test.junit)
                implementation(libs.test.coroutines.debug)
                implementation(libs.test.kotlin.test.junit)
            }
        }

        nativeMain {
            dependsOn(commonMain.get())
        }

        macosMain {
            dependsOn(nativeMain.get())
        }
        val macosX64Main by getting {
            dependsOn(macosMain.get())
        }
        val macosArm64Main by getting {
            dependsOn(macosMain.get())
        }
        val iosX64Main by getting {
            dependsOn(nativeMain.get())
        }
        val iosArm64Main by getting {
            dependsOn(nativeMain.get())
        }
        val iosSimulatorArm64Main by getting {
            dependsOn(nativeMain.get())
        }
    }
}

dependencies {
    add("kspJvmTest", projects.ksp.coreProcessor)
}

ksp {
    arg("Routing_Module_Name", "Voyager")
}
