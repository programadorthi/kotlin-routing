import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

fun Project.applyBasicSetup() {
    configureCodestyle()
    configureTargets()

    kotlin {
        explicitApi()
        jvmToolchain(8)

        setCompilationOptions()
        configureSourceSets()
    }
}


fun KotlinMultiplatformExtension.setCompilationOptions() {
    targets.all {
        compilations.all {
            configureCompilation()
        }
    }
}

fun KotlinMultiplatformExtension.configureSourceSets() {
    sourceSets
        .matching { it.name !in listOf("main", "test") }
        .all {
            val srcDir = if (name.endsWith("Main")) "src" else "test"
            val resourcesPrefix = if (name.endsWith("Test")) "test-" else ""
            val platform = name.dropLast(4)

            kotlin.srcDir("$platform/$srcDir")
            resources.srcDir("$platform/${resourcesPrefix}resources")

            languageSettings.apply {
                progressiveMode = true
            }
        }
}
