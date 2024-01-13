import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JvmVendorSpec
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinCommonOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.targets.js.KotlinJsTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

fun Project.applyBasicSetup() {
    configureTargets()
    setupJvmToolchain()

    kotlin {
        explicitApi()

        setCompilationOptions()
        configureSourceSets()
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
        freeCompilerArgs += "-Xexpect-actual-classes"
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

fun Project.setupJvmToolchain() {
    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = JavaVersion.VERSION_1_8.toString()
        }
    }
}