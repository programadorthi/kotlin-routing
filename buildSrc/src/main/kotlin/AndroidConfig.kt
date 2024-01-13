/*
 * Copyright 2014-2021 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
@file:Suppress("UNUSED_VARIABLE")

import com.android.build.gradle.BaseExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.*

fun Project.configureAndroid() {
    kotlin {
        androidTarget {
            publishLibraryVariants = listOf("release")
        }

        sourceSets.apply {
            val androidMain by getting {
                findByName("commonMain")?.let { dependsOn(it) }
                findByName("jvmAndNixMain")?.let { dependsOn(it) }
                findByName("jvmMain")?.let { dependsOn(it) }
            }

            val androidTest by creating {
                findByName("commonTest")?.let { dependsOn(it) }
                findByName("jvmAndNixTest")?.let { dependsOn(it) }
                findByName("jvmTest")?.let { dependsOn(it) }
            }
        }
    }

    extensions.findByType<BaseExtension>()?.apply {
        compileSdkVersion(34)
        sourceSets["main"].java.srcDirs("android/src/kotlin")
        sourceSets["main"].manifest.srcFile("android/src/AndroidManifest.xml")
        sourceSets["main"].res.srcDirs("android/src/res")

        defaultConfig {
            minSdk = 23
        }

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_1_8
            targetCompatibility = JavaVersion.VERSION_1_8
        }
    }
}
