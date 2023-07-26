/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
import kotlinx.kover.gradle.plugin.dsl.KoverReportExtension
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.tasks.testing.AbstractTestTask
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.kotlin.dsl.*

fun Project.configureCommon() {
    val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

    kotlin {
        sourceSets {
            val commonMain by getting {
                dependencies {
                    api(libs.findLibrary("coroutines-core").get())
                }
            }

            val commonTest by getting {
                dependencies {
                    implementation(kotlin("test"))
                    implementation(libs.findLibrary("test-coroutines").get())
                }
            }
        }
    }

    tasks.withType<AbstractTestTask> {
        testLogging {
            events("PASSED", "FAILED", "SKIPPED")
            exceptionFormat = TestExceptionFormat.FULL
            showStandardStreams = true
            showStackTraces = true
        }
    }

    configure<KoverReportExtension> {
        verify {
            rule {
                bound {
                    minValue = 55 // TODO: increase value
                }
            }
        }
    }
}
