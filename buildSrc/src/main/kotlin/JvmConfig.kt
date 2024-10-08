/*
 * Copyright 2014-2021 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.creating
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.getting
import org.gradle.kotlin.dsl.register
import org.jetbrains.kotlin.gradle.targets.jvm.tasks.KotlinJvmTest

fun Project.configureJvm() {
    val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

    kotlin {
        jvm()

        sourceSets.apply {
            val jvmMain by getting {
                dependencies {
                    api(libs.findLibrary("slf4j-api").get())
                }
            }

            val jvmTest by getting {
                dependencies {
                    implementation(libs.findLibrary("test-junit").get())
                    implementation(libs.findLibrary("test-coroutines-debug").get())
                    implementation(libs.findLibrary("test-kotlin-test-junit").get())
                    implementation(libs.findLibrary("slf4j-simple").get())
                }
            }
        }
    }

    tasks.register<Jar>("jarTest") {
        dependsOn(tasks.getByName("jvmTestClasses"))
        archiveClassifier.set("test")
        from(kotlin.targets.getByName("jvm").compilations.getByName("test").output)
    }

    configurations.apply {
        val testCompile = findByName("testCompile") ?: return@apply

        val testOutput by creating {
            extendsFrom(testCompile)
        }
        val boot by creating {
        }
    }

    val jvmTest: KotlinJvmTest = tasks.getByName<KotlinJvmTest>("jvmTest") {
        ignoreFailures = true
        maxHeapSize = "2g"
        exclude("**/*StressTest *")
    }

    tasks.create<Test>("stressTest") {
        classpath = files(jvmTest.classpath)
        testClassesDirs = files(jvmTest.testClassesDirs)

        ignoreFailures = true
        maxHeapSize = "2g"
        jvmArgs("-XX:+HeapDumpOnOutOfMemoryError")
        setForkEvery(1)
        systemProperty("enable.stress.tests", "true")
        include("**/*StressTest*")
    }
}
