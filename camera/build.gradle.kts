plugins {
    alias(libs.plugins.maxheight.kmp.library)
    alias(libs.plugins.maxheight.lint)
    alias(libs.plugins.maxheight.publishing)
    alias(libs.plugins.maxheight.compose)
}

kotlin {
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Camera"
            isStatic = true
        }
    }

    sourceSets {
        androidInstrumentedTest.dependencies {
            implementation(libs.androidx.testExt.junit)
            implementation(libs.androidx.test.rules)
            implementation(libs.androidx.activity.compose)
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }

        androidMain.dependencies {
            implementation(libs.androidx.camera.camera2)
            implementation(libs.androidx.camera.lifecycle)
            implementation(libs.androidx.camera.view)
            implementation(libs.guava)
        }
    }
}

android {
    namespace = "com.maxheight.camera"
}