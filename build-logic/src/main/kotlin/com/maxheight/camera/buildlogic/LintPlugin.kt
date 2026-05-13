package com.maxheight.camera.buildlogic

import com.android.build.api.dsl.LibraryExtension
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType

class LintPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply(versionCatalog.plugin("detekt"))

            extensions.configure<LibraryExtension> {
                lint {
                    abortOnError = true
                    // Optional: specifically check for issues related to libraries
                    checkReleaseBuilds = true
                    // Generates an HTML/XML report you can view if it fails
                    textReport = true
                    disable += "OldTargetApi"
                }
            }

            extensions.configure<DetektExtension> {
                toolVersion = "1.23.8"
                config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
                source.setFrom(
                    "src/commonMain/kotlin",
                    "src/androidMain/kotlin",
                    "src/iosMain/kotlin"
                )

                buildUponDefaultConfig = true
            }

            tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
                jvmTarget = "21"
            }
        }
    }
}