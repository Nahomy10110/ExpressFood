// Top-level build file
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.kapt) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.sonarqube)
}

sonar {
    properties {
        property("sonar.projectKey", "Nahomy10110_ExpressFood")
        property("sonar.organization", "Nahomy10110")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.projectName", "ExpressFood")
    }
}