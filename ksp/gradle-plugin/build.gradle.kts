plugins {
    kotlin("jvm")
    id("com.gradle.plugin-publish") version "1.3.0"
    id("org.jetbrains.kotlinx.kover")
    alias(libs.plugins.maven.publish)
}

dependencies {
    implementation(libs.plugin.kotlin)
}

gradlePlugin {
    website.set("https://github.com/programadorthi/kotlin-routing")
    vcsUrl.set("https://github.com/programadorthi/kotlin-routing")

    plugins {
        create("kotlin-routing-plugin") {
            id = "dev.programadorthi.routing"
            implementationClass = "dev.programadorthi.routing.gradle.KotlinRoutingGradlePlugin"
            displayName = "Kotlin Routing Gradle Plugin"
            description = "Gradle Plugin for Kotlin Routing"
            tags.set(listOf("router", "kotlin", "kotlin-mpp", "ktor", "routing", "navigation"))
        }
    }
}

if (project.hasProperty("version")) {
    val versionProperty = project.property("version")
    if (versionProperty != "unspecified") {
        project.exec {
            commandLine(
                "find", "./src",
                "-type", "f",
                "-exec",
                "sed", "-i",
                "''", """s/<version>/$versionProperty/g""",
                "{}", "+"
            )
        }
    }
}
