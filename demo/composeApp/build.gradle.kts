plugins {
    alias(libs.plugins.maxheight.kmp.application)
    alias(libs.plugins.maxheight.compose)
}

kotlin {
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.activity.compose)
        }
        commonMain.dependencies {
            implementation(project(":camera"))
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
        }
    }
}

android {
    namespace = "com.maxheight.camera.demo"

    defaultConfig {
        applicationId = "com.maxheight.camera.demo"
        versionCode = 1
        versionName = "1.0"
    }
}
