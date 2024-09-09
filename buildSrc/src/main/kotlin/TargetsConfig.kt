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
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl

val Project.files: Array<File> get() = project.projectDir.listFiles() ?: emptyArray()
val Project.hasCommon: Boolean get() = files.any { it.name == "common" }
val Project.hasJvmAndNix: Boolean get() = hasCommon || files.any { it.name == "jvmAndNix" }
val Project.hasPosix: Boolean get() = hasCommon || files.any { it.name == "posix" }
val Project.hasDesktop: Boolean get() = hasPosix || files.any { it.name == "desktop" }
val Project.hasNix: Boolean get() = hasPosix || hasJvmAndNix || files.any { it.name == "nix" }
val Project.hasLinux: Boolean get() = hasNix || files.any { it.name == "linux" }
val Project.hasDarwin: Boolean get() = hasNix || files.any { it.name == "darwin" }
val Project.hasWindows: Boolean get() = hasPosix || files.any { it.name == "windows" }
val Project.hasJsAndWasmShared: Boolean get() = files.any { it.name == "jsAndWasmShared" }
val Project.hasJs: Boolean get() = hasCommon || files.any { it.name == "js" } || hasJsAndWasmShared
val Project.hasWasm: Boolean get() = hasCommon || files.any { it.name == "wasmJs" } || hasJsAndWasmShared
val Project.hasJvm: Boolean get() = hasCommon || hasJvmAndNix || files.any { it.name == "jvm" }
val Project.hasNative: Boolean get() =
    hasCommon || hasNix || hasPosix || hasLinux || hasDarwin || hasDesktop || hasWindows

fun Project.configureTargets() {
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

        if (hasWasm) {
            @OptIn(ExperimentalWasmDsl::class)
            wasmJs {
                nodejs()
                browser()
            }

            configureWasm()
        }

        if (hasPosix || hasLinux || hasDarwin || hasWindows) extra.set("hasNative", true)

        applyDefaultHierarchyTemplate()

        /*sourceSets {
            val mainSourceSet = { sourceSetName: String ->
                findByName(sourceSetName) ?: commonMain.get()
            }

            val testSourceSet = { sourceSetName: String ->
                findByName(sourceSetName) ?: commonTest.get()
            }

            if (hasJvmAndNix) {
                val jvmAndNixMain by creating {
                    dependsOn(commonMain.get())
                }

                val jvmAndNixTest by creating {
                    dependsOn(commonTest.get())
                }
            }

            if (hasPosix) {
                val posixMain by creating {
                    dependsOn(commonMain.get())
                }

                val posixTest by creating {
                    dependsOn(commonTest.get())
                }
            }

            if (hasDesktop) {
                val desktopMain by creating {
                    dependsOn(mainSourceSet("posixMain"))
                }

                val desktopTest by creating {
                    dependsOn(testSourceSet("posixTest"))
                }
            }

            if (hasNix) {
                val nixMain by creating {
                    findByName("jvmAndNixMain")?.let(::dependsOn)
                    findByName("posixMain")?.let(::dependsOn)
                }

                val nixTest by creating {
                    findByName("jvmAndNixTest")?.let(::dependsOn)
                    findByName("posixTest")?.let(::dependsOn)
                }
            }

            if (hasLinux) {
                linuxMain {
                    val dep = findByName("nixMain") ?: findByName("desktopMain") ?: commonMain.get()
                    dependsOn(dep)
                }

                linuxTest {
                    val dep = findByName("nixTest") ?: findByName("desktopTest") ?: commonTest.get()
                    dependsOn(dep)
                }

                linuxTargets().forEach {
                    getByName("${it}Main").dependsOn(linuxMain.get())
                    getByName("${it}Test").dependsOn(linuxTest.get())
                }
            }

            if (hasDarwin) {
                val darwinMain by creating {
                    dependsOn(commonMain.get())
                }
                val darwinTest by creating {
                    dependsOn(commonTest.get())
                }

                macosMain.get().dependsOn(darwinMain)
                tvosMain.get().dependsOn(darwinMain)
                iosMain.get().dependsOn(darwinMain)
                watchosMain.get().dependsOn(darwinMain)

                macosTest.get().dependsOn(darwinTest)
                tvosTest.get().dependsOn(darwinTest)
                iosTest.get().dependsOn(darwinTest)
                watchosTest.get().dependsOn(darwinTest)

                macosTargets().forEach {
                    getByName("${it}Main").dependsOn(macosMain.get())
                    getByName("${it}Test").dependsOn(macosTest.get())
                }

                iosTargets().forEach {
                    getByName("${it}Main").dependsOn(iosMain.get())
                    getByName("${it}Test").dependsOn(iosTest.get())
                }

                watchosTargets().forEach {
                    getByName("${it}Main").dependsOn(watchosMain.get())
                    getByName("${it}Test").dependsOn(watchosTest.get())
                }

                tvosTargets().forEach {
                    getByName("${it}Main").dependsOn(tvosMain.get())
                    getByName("${it}Test").dependsOn(tvosTest.get())
                }
            }

            if (hasWindows) {
                val windowsMain by creating {
                    val dep = findByName("desktopMain") ?: findByName("posixMain") ?: commonMain.get()
                    dependsOn(dep)
                }

                val windowsTest by creating {
                    val dep = findByName("desktopTest") ?: findByName("posixTest") ?: commonTest.get()
                    dependsOn(dep)
                }

                windowsTargets().forEach {
                    getByName("${it}Main").dependsOn(windowsMain)
                    getByName("${it}Test").dependsOn(windowsTest)
                }
            }

            if (hasJsAndWasmShared) {
                val jsAndWasmSharedMain by creating {
                    dependsOn(commonMain.get())
                }
                val jsAndWasmSharedTest by creating {
                    dependsOn(commonTest.get())
                }
            }

            if (hasJs) {
                jsMain {
                    findByName("jsAndWasmSharedMain")?.let(::dependsOn)
                }
                jsTest {
                    findByName("jsAndWasmSharedTest")?.let(::dependsOn)
                }
            }

            if (hasWasm) {
                wasmJsMain {
                    findByName("jsAndWasmSharedMain")?.let(::dependsOn)
                }
                wasmJsTest {
                    findByName("jsAndWasmSharedTest")?.let(::dependsOn)
                }
            }

            if (hasJvm) {
                jvmMain {
                    findByName("jvmAndNixMain")?.let(::dependsOn)
                }

                jvmTest {
                    findByName("jvmAndNixTest")?.let(::dependsOn)
                }
            }

            if (hasNative) {
                tasks.findByName("linkDebugTestLinuxX64")?.onlyIf { HOST_NAME == "linux" }
                tasks.findByName("linkDebugTestLinuxArm64")?.onlyIf { HOST_NAME == "linux" }
                tasks.findByName("linkDebugTestMingwX64")?.onlyIf { HOST_NAME == "windows" }
            }
        }*/
    }

    if (hasJsAndWasmShared) {
        tasks.configureEach {
            if (name == "compileJsAndWasmSharedMainKotlinMetadata") {
                enabled = false
            }
        }
    }
}
