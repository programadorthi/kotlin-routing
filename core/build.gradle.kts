plugins {
    kotlin("multiplatform")
    id("org.jlleitschuh.gradle.ktlint")
    id("maven-publish")
}

group = "dev.programadorthi"
version = "0.0.1"

kotlin {
    explicitApi()

    jvm {
        jvmToolchain(8)
        withJava()
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }

    js(IR) {
        browser()
    }

    val hostOs = System.getProperty("os.name")
    val isMingwX64 = hostOs.startsWith("Windows")
    val nativeTarget = when {
        hostOs == "Mac OS X" -> macosX64("native")
        hostOs == "Linux" -> linuxX64("native")
        isMingwX64 -> mingwX64("native")
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(libs.coroutines.core)
                api(libs.ktor.http)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.coroutines.test)
            }
        }

        val jvmMain by getting
        val jvmTest by getting

        val jsMain by getting
        val jsTest by getting

        val nativeMain by getting
        val nativeTest by getting
    }
}
