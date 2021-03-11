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
        project.afterEvaluate {
            if (project.rootProject.tasks.findByName("test") != null) {
                return@afterEvaluate
            }
            project.tasks.register("test") {
                it.group = "verification"
                it.dependsOn("allTests")
            }
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
                            this.freeCompilerArgs += listOf("-Xopt-in=kotlin.ExperimentalStdlibApi")

                        }
                    }
                }
                this.nodejs
            }

            mpp.jvm {
                this.compilations.getByName("main").kotlinOptions.apply {
                    jvmTarget = "1.8"
                    freeCompilerArgs += listOf("-Xopt-in=kotlin.ExperimentalStdlibApi")
                }
                this.compilations.getByName("test").kotlinOptions.apply {
                    jvmTarget = "1.8"
                    freeCompilerArgs += listOf("-Xopt-in=kotlin.ExperimentalStdlibApi")
                }
            }

            project.plugins.withId("com.android.library") {
                mpp.android {
                    publishLibraryVariants("release", "debug")
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

                project.plugins.withId("com.android.library") {
                    getByName("androidMain") {
                        it.dependencies {
                            implementation(kotlin("stdlib-jdk8"))
                        }
                    }

                    getByName("androidTest") {
                        it.dependencies {
                            implementation(kotlin("test-junit"))
                        }
                    }
                }
            }
        }

        project.afterEvaluate {
            project.tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class.java).all {
                it.kotlinOptions {
                    jvmTarget = "1.8"
                    freeCompilerArgs += listOf("-Xopt-in=kotlin.ExperimentalStdlibApi")
                }
            }
        }
    }
}
