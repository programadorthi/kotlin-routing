/*
 * Copyright 2014-2023 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
import org.gradle.api.Project
import org.tomlj.*

/**
 * A specific set of versions, read from gradle/libs.versions.toml.
 *
 * The projectDir is required from the project instance because using the relative path doesn't work on some platforms.
 */
val Project.Versions: Versions get() {
    return localVersions ?: run {
        val tomlFile = rootDir.toPath()
            .resolve("gradle/libs.versions.toml")
        val toml = Toml.parse(tomlFile)
        val versions = object {
            operator fun get(key: String): String =
                toml.getString("versions.$key")
                    ?: throw IllegalArgumentException("Version for $key not found in versions catalog $tomlFile")
        }
        Versions(
            versions["puppeteer"],
        )
    }.also {
        localVersions = it
    }
}

private var localVersions: Versions? = null

data class Versions(
    val puppeteer: String,
)