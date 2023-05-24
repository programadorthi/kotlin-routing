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
                implementation("dev.programadorthi.routing:core-stack:0.0.3")
            }
        }

        val nativeTest by getting
    }
}
