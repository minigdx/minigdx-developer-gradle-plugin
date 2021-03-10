package com.github.minigdx.gradle.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Configure project with Kotlin multiplatform
 */
class MiniGdxKotlinMppPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.apply { it.plugin("com.github.minigdx.gradle.plugin.developer") }
        configureKotlinMultiplatform(project)
        configureTestTask(project)
    }

    private fun configureTestTask(project: Project) {
        project.tasks.create("test") {
            it.group = "verification"
            it.dependsOn("allTests")
        }
    }

    private fun configureKotlinMultiplatform(project: Project) {
        project.apply { it.plugin("org.jetbrains.kotlin.multiplatform") }
        project.extensions.configure<KotlinMultiplatformExtension>("kotlin") { mpp ->
            mpp.js {
                this.useCommonJs()
                this.browser {
                    this.webpackTask {
                        this.compilation.kotlinOptions {
                            this.sourceMap = true
                            this.sourceMapEmbedSources = "always"
                        }
                    }
                }
                this.nodejs
            }

            mpp.jvm {
                this.compilations.getByName("main").kotlinOptions.jvmTarget = "1.8"
                this.compilations.getByName("test").kotlinOptions.jvmTarget = "1.8"
            }

            project.plugins.withId("com.android.library") {
                mpp.android {
                    publishLibraryVariants("release", "debug")
                }
            }

            mpp.mingwX64 {
                binaries {
                    staticLib { }
                    sharedLib { }
                }
            }
            mpp.linuxX64 {
                binaries {
                    staticLib { }
                    sharedLib { }
                }

            }
            mpp.ios {
                binaries {
                    staticLib { }
                    sharedLib { }
                }
            }
            mpp.macosX64 {
                binaries {
                    staticLib { }
                    sharedLib { }
                }
            }

            mpp.sourceSets.apply {
                getByName("commonMain") {
                    it.dependencies {
                        implementation(kotlin("stdlib-common"))
                    }
                }

                getByName("commonTest") {
                    it.dependencies {
                        implementation(kotlin("test-common"))
                        implementation(kotlin("test-annotations-common"))
                    }
                }

                getByName("jsMain") {
                    it.dependencies {
                        implementation(kotlin("stdlib-js"))
                    }
                }

                getByName("jsTest") {
                    it.dependencies {
                        implementation(kotlin("test-js"))
                    }
                }

                getByName("jvmMain") {
                    it.dependencies {
                        implementation(kotlin("stdlib-jdk8"))
                    }
                }

                getByName("jvmTest") {
                    it.dependencies {
                        implementation(kotlin("test-junit"))
                    }
                }

            }
        }
    }

}
