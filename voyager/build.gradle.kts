plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("org.jetbrains.compose")
    id("org.jlleitschuh.gradle.ktlint")
    id("maven-publish")
}

group = "dev.programadorthi.routing"
version = "0.0.1"

configureCommon()

// TODO: Voyager has targets limitation. That is the reason to duplicate configs below
kotlin {
    explicitApi()

    setCompilationOptions()
    configureSourceSets()
    setupJvmToolchain()

    jvm("desktop")

    android {
        publishAllLibraryVariants()
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
                compileOnly(libs.compose.runtime)
                compileOnly(libs.compose.runtime.saveable)
                api(libs.voyager.navigator)
            }
        }

        commonTest {
            dependencies {
                implementation(libs.compose.runtime)
                implementation(libs.compose.runtime.saveable)
            }
        }

        val jvmMain by creating {
            dependsOn(commonMain.get())
            dependencies {
                api(libs.slf4j.api)
            }
        }
        val jvmTest by creating {
            dependsOn(commonTest.get())
            dependencies {
                implementation(libs.test.junit)
                implementation(libs.test.coroutines.debug)
                implementation(libs.test.kotlin.test.junit)
            }
        }

        val desktopMain by getting {
            dependsOn(jvmMain)
        }
        val androidMain by getting {
            dependsOn(jvmMain)
            kotlin.srcDir("android/src")
        }
        val desktopTest by getting {
            dependsOn(jvmTest)
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
    compileSdk = 33

    defaultConfig {
        minSdk = 21
        targetSdk = 33
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    sourceSets["main"].manifest.srcFile("android/AndroidManifest.xml")

    packagingOptions {
        exclude("META-INF/kotlinx-coroutines-core.kotlin_module")
    }
}
