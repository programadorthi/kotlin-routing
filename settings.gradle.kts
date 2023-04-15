enableFeaturePreview("VERSION_CATALOGS")
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

include(":core")
include(":core-stack")
include(":resources")
//include(":status-pages")
//include(":voyager")

//include(":samples:android-sample")