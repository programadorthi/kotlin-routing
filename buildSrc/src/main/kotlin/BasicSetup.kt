import org.gradle.api.Project
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.invoke
import org.jetbrains.kotlin.gradle.dsl.KotlinCommonOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.targets.js.KotlinJsTarget

fun Project.applyBasicSetup() {
    group = "dev.programadorthi.routing"
    version = "0.0.1"

    configureTargets()

    kotlin {
        explicitApi()

        setCompilationOptions()
        configureSourceSets()
        setupJvmToolchain()
    }
}


fun KotlinMultiplatformExtension.setCompilationOptions() {
    targets.all {
        if (this is KotlinJsTarget) {
            irTarget?.compilations?.all {
                configureCompilation()
            }
        }
        compilations.all {
            configureCompilation()
        }
    }
}

fun KotlinCompilation<KotlinCommonOptions>.configureCompilation() {
    kotlinOptions {
        if (platformType == KotlinPlatformType.jvm) {
            allWarningsAsErrors = true
        }

        freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
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

    sourceSets {
        findByName("jvmMain")?.kotlin?.srcDirs("jvmAndNix/src")
        findByName("jvmTest")?.kotlin?.srcDirs("jvmAndNix/test")
        findByName("jvmMain")?.resources?.srcDirs("jvmAndNix/resources")
        findByName("jvmTest")?.resources?.srcDirs("jvmAndNix/test-resources")
    }
}

fun Project.setupJvmToolchain() {
    kotlin {
        jvmToolchain {
            languageVersion.set(JavaLanguageVersion.of(8))
        }
    }
}