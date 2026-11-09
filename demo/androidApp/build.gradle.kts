plugins {
    alias(libs.plugins.maxheight.kmp.application)
    alias(libs.plugins.composeCompiler)
}

android {
    namespace = "com.maxheight.camera.demo"

    defaultConfig {
        applicationId = "com.maxheight.camera.demo"
        versionCode = 2
        versionName = "1.1"
    }
}

dependencies {
    implementation(project(":demo:shared"))
    implementation(libs.compose.uiToolingPreview)
    implementation(libs.androidx.activity.compose)
    implementation(libs.compose.runtime)
    debugImplementation(libs.compose.uiTooling)
}