/*
 * Copyright 2014-2021 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.the
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.mpp.DefaultCInteropSettings
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jmailen.gradle.kotlinter.KotlinterExtension

fun Project.kotlin(block: KotlinMultiplatformExtension.() -> Unit) {
    configure(block)
}

val Project.kotlin: KotlinMultiplatformExtension get() = the()

val Project.kotlinter: KotlinterExtension get() = the()

fun KotlinMultiplatformExtension.createCInterop(
    name: String,
    cinteropTargets: List<String>,
    block: DefaultCInteropSettings.() -> Unit
) {
    cinteropTargets.mapNotNull { targets.findByName(it) }.filterIsInstance<KotlinNativeTarget>()
        .forEach {
            val main by it.compilations
            main.cinterops.create(name, block)
        }
}

fun NamedDomainObjectContainer<KotlinSourceSet>.commonMain(block: KotlinSourceSet.() -> Unit) {
    val sourceSet = getByName("commonMain")
    block(sourceSet)
}

fun NamedDomainObjectContainer<KotlinSourceSet>.commonTest(block: KotlinSourceSet.() -> Unit) {
    val sourceSet = getByName("commonTest")
    block(sourceSet)
}

fun NamedDomainObjectContainer<KotlinSourceSet>.jvmAndNixMain(block: KotlinSourceSet.() -> Unit) {
    val sourceSet = findByName("jvmAndNixMain") ?: getByName("jvmMain")
    block(sourceSet)
}

fun NamedDomainObjectContainer<KotlinSourceSet>.jvmAndNixTest(block: KotlinSourceSet.() -> Unit) {
    val sourceSet = findByName("jvmAndNixTest") ?: getByName("jvmTest")
    block(sourceSet)
}

fun NamedDomainObjectContainer<KotlinSourceSet>.nixTest(block: KotlinSourceSet.() -> Unit) {
    val sourceSet = findByName("nixTest") ?: return
    block(sourceSet)
}

fun NamedDomainObjectContainer<KotlinSourceSet>.posixMain(block: KotlinSourceSet.() -> Unit) {
    val sourceSet = findByName("posixMain") ?: return
    block(sourceSet)
}

fun NamedDomainObjectContainer<KotlinSourceSet>.darwinMain(block: KotlinSourceSet.() -> Unit) {
    val sourceSet = findByName("darwinMain") ?: return
    block(sourceSet)
}

fun NamedDomainObjectContainer<KotlinSourceSet>.darwinTest(block: KotlinSourceSet.() -> Unit) {
    val sourceSet = findByName("darwinTest") ?: return
    block(sourceSet)
}

fun NamedDomainObjectContainer<KotlinSourceSet>.desktopMain(block: KotlinSourceSet.() -> Unit) {
    val sourceSet = findByName("desktopMain") ?: return
    block(sourceSet)
}

fun NamedDomainObjectContainer<KotlinSourceSet>.desktopTest(block: KotlinSourceSet.() -> Unit) {
    val sourceSet = findByName("desktopTest") ?: return
    block(sourceSet)
}

fun NamedDomainObjectContainer<KotlinSourceSet>.windowsMain(block: KotlinSourceSet.() -> Unit) {
    val sourceSet = findByName("windowsMain") ?: return
    block(sourceSet)
}

fun NamedDomainObjectContainer<KotlinSourceSet>.windowsTest(block: KotlinSourceSet.() -> Unit) {
    val sourceSet = findByName("windowsTest") ?: return
    block(sourceSet)
}

