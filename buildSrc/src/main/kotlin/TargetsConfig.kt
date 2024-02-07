/*
 * Copyright 2014-2021 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

import org.gradle.api.Project
import org.gradle.kotlin.dsl.creating
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.getting
import org.gradle.kotlin.dsl.invoke
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import java.io.File

val Project.files: Array<File> get() = project.projectDir.listFiles() ?: emptyArray()
val Project.hasCommon: Boolean get() = files.any { it.name == "common" }
val Project.hasJvmAndNix: Boolean get() = hasCommon || files.any { it.name == "jvmAndNix" }
val Project.hasPosix: Boolean get() = hasCommon || files.any { it.name == "posix" }
val Project.hasDesktop: Boolean get() = hasPosix || files.any { it.name == "desktop" }
val Project.hasNix: Boolean get() = hasPosix || hasJvmAndNix || files.any { it.name == "nix" }
val Project.hasDarwin: Boolean get() = hasNix || files.any { it.name == "darwin" }
val Project.hasWindows: Boolean get() = hasPosix || files.any { it.name == "windows" }
val Project.hasJs: Boolean get() = hasCommon || files.any { it.name == "js" }
val Project.hasJvm: Boolean get() = hasCommon || hasJvmAndNix || files.any { it.name == "jvm" }
val Project.hasNative: Boolean get() = hasCommon || hasNix || hasPosix || hasDarwin || hasDesktop || hasWindows

fun Project.configureTargets() {
    val projectName = name
    val specialCharacters = """\W""".toRegex()

    configureCommon()
    if (hasJvm) configureJvm()

    kotlin {
        if (hasJs) {
            js(IR) {
                nodejs()
                browser()
            }

            configureJs()
        }

        if (hasDarwin) {
            listOf(
                macosX64(),
                macosArm64(),
                iosX64(),
                iosArm64(),
                iosSimulatorArm64(),
            ).forEach { iosTarget ->
                val moduleName = projectName
                    .lowercase()
                    .split(specialCharacters)
                    .joinToString(separator = "") { splitName ->
                        splitName.replaceFirstChar { it.uppercase() }
                    }
                iosTarget.binaries.framework {
                    baseName = "${moduleName}Shared"
                }
            }
        }

        if (hasPosix || hasDarwin || hasWindows) extra.set("hasNative", true)

        sourceSets {
            if (hasPosix) {
                val posixMain by creating
                val posixTest by creating
            }

            if (hasNix) {
                val nixMain by creating
                val nixTest by creating
            }

            if (hasDarwin) {
                val darwinMain by creating
                val darwinTest by creating {
                    dependencies {
                        implementation(kotlin("test"))
                    }
                }

                val macosMain by creating
                val macosTest by creating

                val watchosMain by creating
                val watchosTest by creating

                val tvosMain by creating
                val tvosTest by creating

                val iosMain by creating
                val iosTest by creating
            }

            if (hasDesktop) {
                val desktopMain by creating
                val desktopTest by creating {
                    dependencies {
                        implementation(kotlin("test"))
                    }
                }
            }

            if (hasWindows) {
                val windowsMain by creating
                val windowsTest by creating
            }

            if (hasJvmAndNix) {
                val jvmAndNixMain by creating {
                    findByName("commonMain")?.let { dependsOn(it) }
                }

                val jvmAndNixTest by creating {
                    findByName("commonTest")?.let { dependsOn(it) }
                }
            }

            if (hasJvm) {
                val jvmMain by getting {
                    findByName("jvmAndNixMain")?.let { dependsOn(it) }
                }

                val jvmTest by getting {
                    findByName("jvmAndNixTest")?.let { dependsOn(it) }
                }
            }

            if (hasPosix) {
                val posixMain by getting {
                    findByName("commonMain")?.let { dependsOn(it) }
                }

                val posixTest by getting {
                    findByName("commonTest")?.let { dependsOn(it) }

                    dependencies {
                        implementation(kotlin("test"))
                    }
                }

                posixTargets().forEach {
                    getByName("${it}Main").dependsOn(posixMain)
                    getByName("${it}Test").dependsOn(posixTest)
                }
            }

            if (hasNix) {
                val nixMain by getting {
                    findByName("posixMain")?.let { dependsOn(it) }
                    findByName("jvmAndNixMain")?.let { dependsOn(it) }
                }

                val nixTest by getting {
                    findByName("posixTest")?.let { dependsOn(it) }
                    findByName("jvmAndNixTest")?.let { dependsOn(it) }
                }

                nixTargets().forEach {
                    getByName("${it}Main").dependsOn(nixMain)
                    getByName("${it}Test").dependsOn(nixTest)
                }
            }

            if (hasDarwin) {
                val nixMain: KotlinSourceSet? = findByName("nixMain")
                val darwinMain by getting
                val darwinTest by getting
                val macosMain by getting
                val macosTest by getting
                val iosMain by getting
                val iosTest by getting
                val watchosMain by getting
                val watchosTest by getting
                val tvosMain by getting
                val tvosTest by getting

                nixMain?.let { darwinMain.dependsOn(it) }
                macosMain.dependsOn(darwinMain)
                tvosMain.dependsOn(darwinMain)
                iosMain.dependsOn(darwinMain)
                watchosMain.dependsOn(darwinMain)

                macosTargets().forEach {
                    getByName("${it}Main").dependsOn(macosMain)
                    getByName("${it}Test").dependsOn(macosTest)
                }

                iosTargets().forEach {
                    getByName("${it}Main").dependsOn(iosMain)
                    getByName("${it}Test").dependsOn(iosTest)
                }

                watchosTargets().forEach {
                    getByName("${it}Main").dependsOn(watchosMain)
                    getByName("${it}Test").dependsOn(watchosTest)
                }

                tvosTargets().forEach {
                    getByName("${it}Main").dependsOn(tvosMain)
                    getByName("${it}Test").dependsOn(tvosTest)
                }

                darwinTargets().forEach {
                    getByName("${it}Main").dependsOn(darwinMain)
                    getByName("${it}Test").dependsOn(darwinTest)
                }
            }

            if (hasDesktop) {
                val desktopMain by getting {
                    findByName("posixMain")?.let { dependsOn(it) }
                }

                val desktopTest by getting

                desktopTargets().forEach {
                    getByName("${it}Main").dependsOn(desktopMain)
                    getByName("${it}Test").dependsOn(desktopTest)
                }
            }

            if (hasWindows) {
                val windowsMain by getting {
                    findByName("posixMain")?.let { dependsOn(it) }
                }

                val windowsTest by getting {
                    dependencies {
                        implementation(kotlin("test"))
                    }
                }

                windowsTargets().forEach {
                    getByName("${it}Main").dependsOn(windowsMain)
                    getByName("${it}Test").dependsOn(windowsTest)
                }
            }

            if (hasNative) {
                tasks.findByName("linkDebugTestLinuxX64")?.onlyIf { HOST_NAME == "linux" }
                tasks.findByName("linkDebugTestMingwX64")?.onlyIf { HOST_NAME == "windows" }
            }
        }
    }
}
