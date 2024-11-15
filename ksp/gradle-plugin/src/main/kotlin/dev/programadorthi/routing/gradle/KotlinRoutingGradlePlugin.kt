package dev.programadorthi.routing.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinSingleTargetExtension
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import java.util.Locale

@Suppress("unused")
class KotlinRoutingGradlePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            checkNotNull(plugins.findPlugin("com.google.devtools.ksp")) {
                "KSP plugin not found. Please, apply ksp plugin before routing plugin"
            }

            val kex = kotlinExtension
            if (kex is KotlinSingleTargetExtension<*>) {
                dependencies.add("ksp", PROCESSOR)
                return@with
            }

            if (kex is KotlinMultiplatformExtension) {
                kex.targets.configureEach { kTarget ->
                    if (kTarget.platformType.name == "common") {
                        dependencies.add("kspCommonMainMetadata", PROCESSOR)
                        return@configureEach
                    }
                    val capitalizedTargetName =
                        kTarget.targetName.replaceFirstChar {
                            if (it.isLowerCase()) {
                                it.titlecase(Locale.US)
                            } else {
                                it.toString()
                            }
                        }
                    dependencies.add("ksp$capitalizedTargetName", PROCESSOR)

                    if (kTarget.compilations.any { it.name == "test" }) {
                        dependencies.add("ksp${capitalizedTargetName}Test", PROCESSOR)
                    }
                }
            }
        }
    }

    private companion object {
        // Version will be replaced by Gradle Exec Task
        private const val PROCESSOR = "dev.programadorthi.routing:ksp-core-processor:<version>"
    }
}
