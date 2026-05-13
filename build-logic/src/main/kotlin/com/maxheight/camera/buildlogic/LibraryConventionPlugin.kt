package com.maxheight.camera.buildlogic

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeTest


class LibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply(versionCatalog.plugin("kotlinMultiplatform"))
                apply(versionCatalog.plugin("androidLibrary"))
            }

            configureKotlinMultiplatform()

            extensions.configure<LibraryExtension> {
                configureAndroidCommon(this)
                defaultConfig {
                    minSdk = versionCatalog.minSdk
                    targetSdk = versionCatalog.targetSdk
                    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                }

                testOptions {
                    unitTests.all {
                        it.testLogging {
                            events("passed", "skipped", "failed", "standardOut", "standardError")
                            showExceptions = true
                            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
                            showCauses = true
                            showStackTraces = true
                        }
                    }
                }
            }

            tasks.withType<KotlinNativeTest> {
                testLogging {
                    events("passed", "skipped", "failed", "standardOut", "standardError")
                    exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
                    showCauses = true
                    showStackTraces = true
                }
            }
        }
    }
}