/*
 * Copyright 2014-2021 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

fun Project.fastOr(block: () -> List<String>): List<String> {
    return block()
}

fun Project.posixTargets(): List<String> = fastOr {
    nixTargets() + windowsTargets()
}

fun Project.nixTargets(): List<String> = fastOr {
    darwinTargets() + kotlin.linuxX64().name
}

fun Project.darwinTargets(): List<String> = fastOr {
    macosTargets() + iosTargets() + watchosTargets() + tvosTargets()
}

fun Project.macosTargets(): List<String> = fastOr {
    with(kotlin) {
        listOf(
            macosX64(),
            macosArm64()
        ).map { it.name }
    }
}

fun Project.iosTargets(): List<String> = fastOr {
    with(kotlin) {
        listOf(
            iosX64(),
            iosArm64(),
            iosSimulatorArm64(),
        ).map { it.name }
    }
}

fun Project.watchosTargets(): List<String> = fastOr {
    with(kotlin) {
        listOf(
            watchosX64(),
            watchosArm32(),
            watchosArm64(),
            watchosSimulatorArm64(),
        ).map { it.name }
    }
}

fun Project.tvosTargets(): List<String> = fastOr {
    with(kotlin) {
        listOf(
            tvosX64(),
            tvosArm64(),
            tvosSimulatorArm64(),
        ).map { it.name }
    }
}

fun Project.desktopTargets(): List<String> = fastOr {
    with(kotlin) {
        listOf(
            macosX64(),
            macosArm64(),
            linuxX64(),
            mingwX64()
        ).map { it.name }
    }
}

fun Project.windowsTargets(): List<String> = fastOr {
    with(kotlin) {
        listOf(
            mingwX64()
        ).map { it.name }
    }
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
