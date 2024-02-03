plugins {
    kotlin("multiplatform")
}

kotlin {
    js {
        binaries.executable()
        browser {
            commonWebpackConfig {
                cssSupport {
                    enabled.set(true)
                }
            }
        }
    }

    sourceSets {
        val jsMain by getting {
            dependencies {
                implementation(projects.integration.javascript)
                
                implementation("org.jetbrains.kotlinx:kotlinx-html-js:0.10.1")
                implementation(libs.serialization.json)
            }
        }
    }
}
