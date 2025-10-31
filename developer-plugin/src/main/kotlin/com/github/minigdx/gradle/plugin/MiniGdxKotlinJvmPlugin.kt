package com.github.minigdx.gradle.plugin

import com.github.minigdx.gradle.plugin.internal.Constants.JAVA_VERSION
import com.github.minigdx.gradle.plugin.internal.Constants.JVM_TARGET
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.tasks.Jar
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaToolchainService
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask
import org.jetbrains.kotlin.gradle.tasks.UsesKotlinJavaToolchain

/**
 * Configure projects using only Kotlin for the JVM
 */
class MiniGdxKotlinJvmPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.apply { it.plugin("com.github.minigdx.gradle.plugin.developer") }
        configureJava(project)
        configureKotlin(project)
        configurePublication(project)
    }

    private fun configureJava(project: Project) {
        // Ensure "org.gradle.jvm.version" is set to "17" in Gradle metadata.
        project.afterEvaluate {
            val toolchainService = project.extensions.getByType(JavaToolchainService::class.java)
            project.tasks.withType(JavaCompile::class.java).configureEach {
                it.sourceCompatibility = JVM_TARGET.target
                it.targetCompatibility = JVM_TARGET.target
                val javaCompiler = toolchainService.compilerFor {
                    it.languageVersion.set(JavaLanguageVersion.of(JAVA_VERSION))
                }
                it.javaCompiler.set(javaCompiler)
            }
        }

        val projectName = project.name
        val projectVersion = project.version.toString()

        project.tasks.withType(Jar::class.java).configureEach {

            it.manifest {
                it.attributes(
                    mapOf(
                        "Implementation-Title" to projectName,
                        "Implementation-Version" to projectVersion
                    )
                )
            }
        }
    }

    private fun configurePublication(project: Project) {
        project.afterEvaluate {
            project.extensions.configure(PublishingExtension::class.java) {
                it.publications {
                    it.create("minigdxForMaven", MavenPublication::class.java) {
                        it.from(project.components.getByName("kotlin"))
                        it.artifact(project.tasks.getByName("kotlinSourcesJar"))
                    }
                }
            }
        }
    }

    private fun configureKotlin(project: Project) {
        project.apply { it.plugin("org.jetbrains.kotlin.jvm") }
        project.dependencies.add("api", project.dependencies.platform("org.jetbrains.kotlin:kotlin-bom"))
        project.dependencies.add("implementation", "org.jetbrains.kotlin:kotlin-stdlib-jdk8")
        project.dependencies.add("implementation", "org.jetbrains.kotlin:kotlin-reflect")
        project.dependencies.add("testImplementation", "org.jetbrains.kotlin:kotlin-test-junit")

        project.plugins.withId("java-test-fixtures") {
            project.dependencies.add("testFixturesImplementation", "org.jetbrains.kotlin:kotlin-stdlib-jdk8")
            project.dependencies.add("testFixturesImplementation", "org.jetbrains.kotlin:kotlin-reflect")
        }

        project.tasks.withType(KotlinCompilationTask::class.java).configureEach {
            it.compilerOptions {
                freeCompilerArgs.addAll(COMPILATION_FLAGS)
            }
            it as UsesKotlinJavaToolchain
            val toolchainService = project.extensions.getByType(JavaToolchainService::class.java)
            it.kotlinJavaToolchain.toolchain.use(
                toolchainService.launcherFor {
                    it.languageVersion.set(JavaLanguageVersion.of(JAVA_VERSION))
                }
            )
        }
    }

    companion object {

        private val COMPILATION_FLAGS = listOf(
            "-Xjsr305=strict",
            "-opt-in=kotlin.ExperimentalStdlibApi",
        )
    }
}
