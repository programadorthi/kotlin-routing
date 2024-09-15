import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.android.library")
    kotlin("android")
    id("org.jetbrains.kotlinx.kover")
    alias(libs.plugins.maven.publish)
}

android {
    compileSdk = 34
    namespace = "dev.programadorthi.routing.android"

    defaultConfig {
        minSdk = 23
        consumerProguardFiles("consumer-rules.pro")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
        freeCompilerArgs += listOf("-Xexplicit-api=strict")
    }
}

dependencies {
    api(projects.resources)
    implementation(libs.androidx.startup)

    testImplementation(kotlin("test"))
    testImplementation(libs.test.junit)
    testImplementation(libs.test.coroutines)
    testImplementation(libs.test.coroutines.debug)
    testImplementation(libs.test.kotlin.test.junit)
    testImplementation(libs.test.robolectric)
}
