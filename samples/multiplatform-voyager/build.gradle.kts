import org.jetbrains.compose.desktop.application.dsl.TargetFormat.Deb
import org.jetbrains.compose.desktop.application.dsl.TargetFormat.Dmg
import org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi
import org.jetbrains.compose.experimental.dsl.IOSDevices
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization") // required for type-safe routing
    id("com.android.application")
    id("org.jetbrains.compose")
}

kotlin {
    android()

    jvm("desktop")

    js(IR) {
        browser()
        binaries.executable()
    }

    val macOsConfiguation: KotlinNativeTarget.() -> Unit = {
        binaries {
            executable {
                entryPoint = "main"
                freeCompilerArgs += listOf(
                    "-linker-option", "-framework", "-linker-option", "Metal"
                )
            }
        }
    }
    macosX64(macOsConfiguation)
    macosArm64(macOsConfiguation)
    val uikitConfiguration: KotlinNativeTarget.() -> Unit = {
        binaries {
            executable() {
                entryPoint = "main"
                freeCompilerArgs += listOf(
                    "-linker-option", "-framework", "-linker-option", "Metal",
                    "-linker-option", "-framework", "-linker-option", "CoreText",
                    "-linker-option", "-framework", "-linker-option", "CoreGraphics"
                )
            }
        }
    }
    ios("uikit")
    iosX64("uikitX64", uikitConfiguration)
    iosArm64("uikitArm64", uikitConfiguration)
    iosSimulatorArm64("uikitSimulatorArm64", uikitConfiguration)

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.material)
                implementation(compose.runtime)

                implementation("dev.programadorthi.routing:voyager:0.0.3")
            }
        }

        val desktopMain by getting {
            dependsOn(commonMain)
            dependencies {
                implementation(compose.desktop.currentOs)
            }
        }

        val androidMain by getting {
            dependsOn(commonMain)
            dependencies {
                implementation(libs.core.ktx)
                implementation(libs.lifecycle.runtime.ktx)
                implementation(libs.activity.appcompat)
                implementation(libs.activity.compose)
            }
        }

        val jsMain by getting  {
            dependencies { }
        }

        val nativeMain by creating {
            dependsOn(commonMain)
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
        val iosMain = getByName("uikitMain").apply {
            dependsOn(nativeMain)
        }
        val iosSimulatorArm64Main = getByName("uikitSimulatorArm64Main").apply {
            dependsOn(iosMain)
        }
    }
}

compose.desktop {
    application {
        mainClass = "dev.programadorthi.routing.voyager.AppKt"
        nativeDistributions {
            targetFormats(Dmg, Msi, Deb)
            packageName = "jvm"
            packageVersion = "1.0.0"
        }
    }
}

compose.experimental {
    web.application {}

    uikit.application {
        bundleIdPrefix = "dev.programadorthi.routing.voyager"
        projectName = "RoutingVoyagerApplication"
        deployConfigurations {
            simulator("IPhone8") {
                device = IOSDevices.IPHONE_8
            }
            simulator("IPad") {
                device = IOSDevices.IPAD_MINI_6th_Gen
            }
        }
    }
}

android {
    compileSdk = 33

    defaultConfig {
        applicationId = "dev.programadorthi.routing.voyager.application"
        minSdk = 21
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

compose.desktop.nativeApplication {
    targets(kotlin.targets.getByName("macosX64"))
    distributions {
        targetFormats(Dmg)
        packageName = "RoutingVoyagerApplication"
        packageVersion = "1.0.0"
    }
}
