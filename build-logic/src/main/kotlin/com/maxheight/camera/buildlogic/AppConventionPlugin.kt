package com.maxheight.camera.buildlogic

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AppConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply(versionCatalog.plugin("androidApplication"))
            }

            extensions.configure<ApplicationExtension> {
                compileSdk = versionCatalog.compileSdk

                compileOptions.apply {
                    sourceCompatibility = versionCatalog.javaVersion()
                    targetCompatibility = versionCatalog.javaVersion()
                }

                packaging.apply {
                    resources {
                        excludes += "/META-INF/{AL2.0,LGPL2.1}"
                    }
                }

                buildTypes.getByName("release") {
                    isMinifyEnabled = false
                }

                defaultConfig {
                    minSdk = versionCatalog.minSdk
                    targetSdk = versionCatalog.targetSdk
                }
            }
        }
    }
}