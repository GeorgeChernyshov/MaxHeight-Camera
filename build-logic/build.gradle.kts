plugins {
    `kotlin-dsl`
    alias(libs.plugins.kotlinJvm)
}

group = "com.maxheight.camera.buildlogic"

dependencies {
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.detekt.gradlePlugin)
    implementation(libs.publish.gradlePlugin)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_2)
        languageVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_2)
    }
}

gradlePlugin {
    plugins {
        register("compose") {
            id = libs.plugins.maxheight.compose.get().pluginId
            implementationClass = "com.maxheight.camera.buildlogic.ComposePlugin"
        }

        register("kmpApplication") {
            id = libs.plugins.maxheight.kmp.application.get().pluginId
            implementationClass = "com.maxheight.camera.buildlogic.AppConventionPlugin"
        }

        register("kmpLibrary") {
            id = libs.plugins.maxheight.kmp.library.get().pluginId
            implementationClass = "com.maxheight.camera.buildlogic.LibraryConventionPlugin"
        }

        register("lint") {
            id = libs.plugins.maxheight.lint.get().pluginId
            implementationClass = "com.maxheight.camera.buildlogic.LintPlugin"
        }

        register("publishing") {
            id = libs.plugins.maxheight.publishing.get().pluginId
            implementationClass = "com.maxheight.camera.buildlogic.PublishingPlugin"
        }
    }
}