plugins {
    alias(libs.plugins.ksp)
    kotlin("jvm")
}

dependencies {
    implementation(projects.core)
    implementation(projects.kspAnnotations)
    implementation(projects.kspProcessor)
    ksp(projects.kspProcessor)
}
