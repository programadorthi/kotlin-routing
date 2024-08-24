/*
 * Copyright 2014-2021 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

import java.io.File
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.getting
import org.gradle.kotlin.dsl.invoke
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl

fun Project.configureWasm() {
    configureWasmTasks()

    kotlin {
        sourceSets {
            val wasmJsTest by getting {
                dependencies {
                    implementation(npm("puppeteer", Versions.puppeteer))
                }
            }
        }
    }

    configureWasmTestTasks()
}

private fun Project.configureWasmTasks() {
    kotlin {
        @OptIn(ExperimentalWasmDsl::class)
        wasmJs {
            nodejs {
                testTask {
                    useMocha {
                        timeout = "10000"
                    }
                }
            }

            browser {
                testTask {
                    useKarma {
                        useChromeHeadlessWasmGc()
                        useConfigDirectory(File(project.rootProject.projectDir, "karma"))
                    }
                }
            }
        }
    }
}

private fun Project.configureWasmTestTasks() {
    val shouldRunWasmBrowserTest = !hasProperty("teamcity") || hasProperty("enable-js-tests")
    if (shouldRunWasmBrowserTest) return

    val cleanWasmJsBrowserTest by tasks.getting
    val wasmJsBrowserTest by tasks.getting
    cleanWasmJsBrowserTest.onlyIf { false }
    wasmJsBrowserTest.onlyIf { false }
}