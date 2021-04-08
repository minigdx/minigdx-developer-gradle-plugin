package com.github.minigdx.gradle.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile
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
                this.compilations.all {
                    it.kotlinOptions {
                        freeCompilerArgs += COMPILATION_FLAGS
                    }
                }
                this.useCommonJs()
                this.browser {
                    this.webpackTask {
                        this.compilation.kotlinOptions {
                            this.sourceMap = true
                            this.sourceMapEmbedSources = "always"
                        }
                    }
                }
                this.nodejs()
            }

            mpp.jvm {
                this.compilations.getByName("main").kotlinOptions.apply {
                    jvmTarget = "1.8"
                    freeCompilerArgs += COMPILATION_FLAGS
                }
                this.compilations.getByName("test").kotlinOptions.apply {
                    jvmTarget = "1.8"
                    freeCompilerArgs += COMPILATION_FLAGS
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
            mpp.sourceSets.all {
                it.languageSettings.apply {
                    this.useExperimentalAnnotation("kotlin.ExperimentalStdlibApi")
                    this.useExperimentalAnnotation("kotlinx.serialization.ExperimentalSerializationApi")
                }
            }
        }

        project.afterEvaluate {
            project.tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class.java).all {
                it.kotlinOptions {
                    jvmTarget = "1.8"
                    freeCompilerArgs += COMPILATION_FLAGS
                }
            }

            project.tasks.withType(JavaCompile::class.java) {
                it.sourceCompatibility = "1.8"
                it.targetCompatibility = "1.8"
            }
        }
    }

    companion object {

        private val COMPILATION_FLAGS = listOf(
            "-Xopt-in=kotlin.ExperimentalStdlibApi",
            "-Xopt-in=kotlinx.serialization.ExperimentalSerializationApi"
        )
    }
}
