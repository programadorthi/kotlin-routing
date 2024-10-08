/*
 * Copyright 2014-2021 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

fun Project.posixTargets(): List<String> = nixTargets() + windowsTargets()

fun Project.nixTargets(): List<String> = darwinTargets() + linuxTargets()

fun Project.linuxTargets(): List<String> = with(kotlin) {
    listOf(
        linuxX64(),
        linuxArm64(),
    )
}.map { it.name }

fun Project.darwinTargets(): List<String> = macosTargets() + iosTargets() + watchosTargets() + tvosTargets()

fun Project.macosTargets(): List<String> = with(kotlin) {
    listOf(
        macosX64(),
        macosArm64()
    ).map { it.name }
}

fun Project.iosTargets(): List<String> = with(kotlin) {
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).map { it.name }
}

fun Project.watchosTargets(): List<String> = with(kotlin) {
    listOf(
        watchosX64(),
        watchosArm32(),
        watchosArm64(),
        watchosSimulatorArm64(),
    ).map { it.name }
}

fun Project.tvosTargets(): List<String> = with(kotlin) {
    listOf(
        tvosX64(),
        tvosArm64(),
        tvosSimulatorArm64(),
    ).map { it.name }
}

fun Project.desktopTargets(): List<String> = with(kotlin) {
    listOf(
        macosX64(),
        macosArm64(),
        linuxX64(),
        linuxArm64(),
        mingwX64()
    ).map { it.name }
}

fun Project.windowsTargets(): List<String> = with(kotlin) {
    listOf(
        mingwX64()
    ).map { it.name }
}

fun Project.darwinTargetsFramework(
    targets: KotlinMultiplatformExtension.() -> List<KotlinNativeTarget> = {
        listOf(
            macosX64(),
            macosArm64(),
            iosX64(),
            iosArm64(),
            iosSimulatorArm64(),
        )
    },
    action: Framework.() -> Unit = {}
) {
    val projectName = name
    val specialCharacters = """\W""".toRegex()

    with(kotlin) {
        targets().forEach { iosTarget ->
            val moduleName = projectName
                .lowercase()
                .split(specialCharacters)
                .joinToString(separator = "") { splitName ->
                    splitName.replaceFirstChar { it.uppercase() }
                }
            iosTarget.binaries.framework {
                baseName = "${moduleName}Shared"
                action()
            }
        }
    }
}
