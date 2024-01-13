plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("com.android.library")
    alias(libs.plugins.jetbrains.compose)
    id("org.jlleitschuh.gradle.ktlint")
    id("org.jetbrains.kotlinx.kover")
    alias(libs.plugins.maven.publish)
}

configureCommon()
configureJvm()
configureAndroid()
setupJvmToolchain()

// TODO: Voyager has targets limitation. That is the reason to duplicate configs below
kotlin {
    explicitApi()

    setCompilationOptions()
    configureSourceSets()

    android {
        publishLibraryVariants("release")
    }

    js(IR) {
        nodejs()
        browser()
    }

    configureJs()

    macosX64()
    macosArm64()
    ios()
    iosSimulatorArm64()

    sourceSets {
        commonMain {
            dependencies {
                api(projects.core)
                api(libs.ktor.resources)
                api(libs.serialization.core)
                api(libs.voyager.navigator)
                implementation(libs.compose.runtime)
            }
        }

        commonTest {
            dependencies {
                implementation(libs.compose.saveable)
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

        val androidMain by getting {
            dependsOn(jvmMain)
            kotlin.srcDir("android/src")
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
        val iosMain by getting {
            dependsOn(nativeMain)
        }
        val iosSimulatorArm64Main by getting {
            dependsOn(iosMain)
        }
    }
}

android {
    compileSdk = 34
    namespace = "dev.programadorthi.routing.voyager"

    defaultConfig {
        minSdk = 23
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    sourceSets["main"].manifest.srcFile("android/AndroidManifest.xml")
}
