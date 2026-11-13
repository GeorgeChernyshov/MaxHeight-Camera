package com.maxheight.camera.buildlogic

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.Test
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeTest


class LibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply(versionCatalog.plugin("kotlinMultiplatform"))
                apply(versionCatalog.plugin("androidLibrary"))
            }

            extensions.configure<KotlinMultiplatformExtension> {
                targets.withType<KotlinMultiplatformAndroidLibraryTarget>()
                    .configureEach {
                        compileSdk = versionCatalog.compileSdk
                        minSdk = versionCatalog.minSdk

                        compilerOptions {
                            jvmTarget.set(versionCatalog.jvmTarget())
                        }

                        packaging.apply {
                            resources {
                                excludes += "/META-INF/{AL2.0,LGPL2.1}"
                            }
                        }

                        withHostTestBuilder {}
                        withDeviceTestBuilder {
                            sourceSetTreeName = "test"
                        }.configure {
                            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                        }
                    }

                iosX64()
                iosArm64()
                iosSimulatorArm64()

                tasks.withType<KotlinNativeTest>().configureEach {
                    testLogging {
                        events("passed", "skipped", "failed", "standardOut", "standardError")
                        exceptionFormat = TestExceptionFormat.FULL
                        showCauses = true
                        showStackTraces = true
                    }
                }

                tasks.withType<Test>().configureEach {
                    testLogging {
                        events("passed", "skipped", "failed", "standardOut", "standardError")
                        showExceptions = true
                        exceptionFormat = TestExceptionFormat.FULL
                        showCauses = true
                        showStackTraces = true
                    }
                }

                sourceSets.apply {
                    commonTest.dependencies {
                        implementation(versionCatalog.library("kotlin-test"))
                    }
                }
            }
        }
    }
}