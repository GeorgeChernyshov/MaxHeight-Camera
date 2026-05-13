package com.maxheight.camera.buildlogic

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AppConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply(versionCatalog.plugin("kotlinMultiplatform"))
                apply(versionCatalog.plugin("androidApplication"))
            }

            configureKotlinMultiplatform()

            extensions.configure<ApplicationExtension> {
                configureAndroidCommon(this)
                defaultConfig {
                    minSdk = versionCatalog.minSdk
                    targetSdk = versionCatalog.targetSdk
                }
            }
        }
    }
}