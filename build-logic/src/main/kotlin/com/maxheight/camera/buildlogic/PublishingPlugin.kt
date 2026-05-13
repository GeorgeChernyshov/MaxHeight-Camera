package com.maxheight.camera.buildlogic

import com.vanniktech.maven.publish.DeploymentValidation
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import kotlin.text.set

class PublishingPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply(versionCatalog.plugin("vanniktechPublish"))

            extensions.configure<MavenPublishBaseExtension> {
                publishToMavenCentral(automaticRelease = true, validateDeployment = DeploymentValidation.PUBLISHED)

                signAllPublications()

                coordinates(
                    groupId = "io.github.georgechernyshov",
                    artifactId = "camera",
                    version = System.getenv("GITHUB_REF_NAME") ?: "1.0.0-SNAPSHOT"
                )

                pom {
                    name.set("MaxHeight-Camera")
                    description.set("Multiplatform library that handles Camera and composable CameraPreview for you!")
                    inceptionYear.set("2026")
                    url.set("https://github.com/GeorgeChernyshov/MaxHeight-Camera")

                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                            distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }

                    developers {
                        developer {
                            id.set("MaxHeight")
                            name.set("George")
                            url.set("https://github.com/GeorgeChernyshov/")
                        }
                    }

                    scm {
                        url.set("https://github.com/GeorgeChernyshov/MaxHeight-Camera")
                        connection.set("scm:git:git://github.com/GeorgeChernyshov/MaxHeight-Camera.git")
                        developerConnection.set("scm:git:ssh://git@github.com/GeorgeChernyshov/MaxHeight-Camera.git")
                    }
                }
            }
        }
    }
}