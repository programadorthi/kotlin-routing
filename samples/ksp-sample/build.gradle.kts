plugins {
    alias(libs.plugins.ksp)
    kotlin("jvm")
}

dependencies {
    implementation(projects.core)
    implementation(projects.ksp.coreAnnotations)
    implementation(projects.ksp.coreProcessor)
    ksp(projects.ksp.coreProcessor)
}
