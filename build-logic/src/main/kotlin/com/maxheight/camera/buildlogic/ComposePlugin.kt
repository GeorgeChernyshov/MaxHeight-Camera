package com.maxheight.camera.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class ComposePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply(versionCatalog.plugin("composeMultiplatform"))
                apply(versionCatalog.plugin("composeCompiler"))
            }

            extensions.configure<KotlinMultiplatformExtension> {
                sourceSets.named("commonMain").configure {
                    dependencies {
                        implementation(versionCatalog.library("compose-runtime"))
                        implementation(versionCatalog.library("compose-foundation"))
                    }
                }
            }

            dependencies.add(
                "debugImplementation",
                versionCatalog.library("compose-uiTooling")
            )
        }
    }
}