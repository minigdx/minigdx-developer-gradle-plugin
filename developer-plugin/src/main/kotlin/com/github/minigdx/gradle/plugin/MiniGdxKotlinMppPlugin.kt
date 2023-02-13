package com.github.minigdx.gradle.plugin

import com.github.minigdx.gradle.plugin.internal.Constants.JAVA_VERSION
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaToolchainService
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinJsCompilerType
import org.jetbrains.kotlin.gradle.tasks.UsesKotlinJavaToolchain

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
            mpp.js(KotlinJsCompilerType.IR) {
                this.binaries.executable()
                this.compilations.all {
                    it.kotlinOptions {
                        freeCompilerArgs = freeCompilerArgs + COMPILATION_FLAGS
                    }
                }
                this.browser {
                    this.webpackTask {
                        this.compilation.kotlinOptions {
                            this.sourceMap = true
                            this.sourceMapEmbedSources = "always"
                        }
                    }
                }
            }

            mpp.jvm {
                this.compilations.getByName("main").kotlinOptions.apply {
                    jvmTarget = "1.8"
                    freeCompilerArgs = freeCompilerArgs + COMPILATION_FLAGS
                }
                this.compilations.getByName("test").kotlinOptions.apply {
                    jvmTarget = "1.8"
                    freeCompilerArgs = freeCompilerArgs + COMPILATION_FLAGS
                }
            }

            if (project.findProperty(MiniGdxDeveloperExtension.IOS_MPP_PROPERTY) == "true") {
                mpp.ios()
                mpp.iosSimulatorArm64()
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

                if (project.findProperty(MiniGdxDeveloperExtension.IOS_MPP_PROPERTY) == "true") {
                    val iosMain = getByName("iosMain") {

                    }

                    val iosTest = getByName("iosTest") {

                    }

                    val iosSimulatorArm64Main = getByName("iosSimulatorArm64Main") {

                    }

                    val iosSimulatorArm64Test = getByName("iosSimulatorArm64Test") {

                    }

                    // Set up dependencies between the source sets
                    iosSimulatorArm64Main.dependsOn(iosMain)
                    iosSimulatorArm64Test.dependsOn(iosTest)
                }

                project.plugins.withId("com.android.library") {
                    getByName("androidMain") {
                        it.dependencies {
                            implementation(kotlin("stdlib-jdk8"))
                        }
                    }

                    getByName("androidUnitTest") {
                        it.dependencies {
                            implementation(kotlin("test-junit"))
                        }
                    }
                }
            }
            mpp.sourceSets.all {
                it.languageSettings.apply {
                    this.optIn("kotlin.ExperimentalStdlibApi")
                    this.optIn("kotlinx.serialization.ExperimentalSerializationApi")
                }
            }
        }

        project.afterEvaluate {
            val toolchainService = project.extensions.getByType(JavaToolchainService::class.java)
            project.tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class.java).configureEach {
                it as UsesKotlinJavaToolchain
                it.kotlinJavaToolchain.toolchain.use(
                    toolchainService.launcherFor {
                        it.languageVersion.set(JavaLanguageVersion.of(JAVA_VERSION))
                    }
                )
                it.kotlinOptions {
                    jvmTarget = "1.8"
                    freeCompilerArgs = freeCompilerArgs + COMPILATION_FLAGS
                }
            }

            project.tasks.withType(JavaCompile::class.java).configureEach {
                it.sourceCompatibility = "1.8"
                it.targetCompatibility = "1.8"
                val javaCompiler = toolchainService.compilerFor {
                    it.languageVersion.set(JavaLanguageVersion.of(JAVA_VERSION)) }
                it.javaCompiler.set(javaCompiler)
            }
        }
    }

    companion object {

        private val COMPILATION_FLAGS = listOf(
            "-opt-in=kotlin.ExperimentalStdlibApi",
            "-opt-in=kotlinx.serialization.ExperimentalSerializationApi"
        )
    }
}
