enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        google()
    }
}

dependencyResolutionManagement {
    // FIXME: a bug with js node resolution
//    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven(url = "https://maven.pkg.jetbrains.space/public/p/compose/dev" )
        google()
    }
}

rootProject.name = "kotlin-routing"

include(":auth")
include(":call-logging")
include(":core")
include(":events")
include(":events-resources")
include(":resources")
include(":sessions")
include(":status-pages")

include(":integration:android")
include(":integration:compose")
include(":integration:compose-animation")
include(":integration:javascript")
include(":integration:voyager")

// Samples are disabled by default to avoid sync their.
//include(":samples:android-activity")
//include(":samples:android-sample")
//include(":samples:multiplatform-voyager")
//include(":samples:kotlin-js-sample")
//include(":samples:win32-sample")