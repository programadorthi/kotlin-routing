import com.android.build.gradle.internal.tasks.factory.dependsOn

plugins {
    kotlin("jvm")
    id("java-gradle-plugin")
    id("com.gradle.plugin-publish") version "1.2.1"
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

val versionRegister = tasks.register<Exec>("version-register") {
    commandLine(
        "find", "./src",
        "-type", "f",
        "-exec",
        "sed", "-i",
        "''", """s/<version>/${providers.gradleProperty("version").get()}/g""",
        "{}", "+"
    )
}

tasks.named("processResources").dependsOn(versionRegister)
