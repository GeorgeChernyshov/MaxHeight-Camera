package com.maxheight.camera.buildlogic

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Shared Kotlin Multiplatform target configuration
 */
internal fun Project.configureKotlinMultiplatform() {
    extensions.configure<KotlinMultiplatformExtension> {
        androidTarget {
            compilerOptions {
                jvmTarget.set(versionCatalog.jvmTarget())
            }
        }
        iosX64()
        iosArm64()
        iosSimulatorArm64()

        sourceSets.apply {
            commonTest.dependencies {
                implementation(versionCatalog.library("kotlin-test"))
            }
        }
    }
}

/**
 * Shared Android configuration (CompileOptions, Packaging, etc.)
 * Uses CommonExtension to work for both Libraries and Applications.
 */
internal fun Project.configureAndroidCommon(
    commonExtension: CommonExtension<*, *, *, *, *, *>
) {
    commonExtension.apply {
        compileSdk = versionCatalog.compileSdk

        compileOptions {
            sourceCompatibility = versionCatalog.javaVersion()
            targetCompatibility = versionCatalog.javaVersion()
        }

        packaging {
            resources {
                excludes += "/META-INF/{AL2.0,LGPL2.1}"
            }
        }

        buildTypes {
            getByName("release") {
                isMinifyEnabled = false
            }
        }
    }
}