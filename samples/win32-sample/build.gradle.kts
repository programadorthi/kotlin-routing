plugins {
    kotlin("multiplatform")
}

kotlin {
    mingwX64("native") {
        binaries {
            executable {
                entryPoint = "main"
            }
        }
    }

    sourceSets {
        val nativeMain by getting {
            dependencies {
                implementation(projects.coreStack)
            }
        }

        val nativeTest by getting
    }
}
