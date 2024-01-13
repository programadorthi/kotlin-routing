/*
 * Copyright 2014-2021 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

import com.android.build.gradle.BaseExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.get

fun Project.configureAndroid() {
    kotlin {
        androidTarget {
            publishLibraryVariants = listOf("release")
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
