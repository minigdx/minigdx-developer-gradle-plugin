package com.github.minigdx.gradle.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.compile.JavaCompile
import org.jetbrains.kotlin.gradle.dsl.KotlinCompile
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions

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
        // Ensure "org.gradle.jvm.version" is set to "8" in Gradle metadata.
        project.afterEvaluate {
            project.tasks.withType(JavaCompile::class.java) {
                it.sourceCompatibility = "1.8"
                it.targetCompatibility = "1.8"
            }
        }
    }

    private fun configurePublication(project: Project) {
        project.extensions.configure(PublishingExtension::class.java) {
            it.publications {
                it.create("maven", MavenPublication::class.java) {
                    it.from(project.components.getByName("kotlin"))
                    it.artifact(project.tasks.getByName("kotlinSourcesJar"))
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

        project.tasks.withType(KotlinCompile::class.java).forEach {
            it.kotlinOptions {
                this as KotlinJvmOptions
                this.jvmTarget = "1.8"
                this.freeCompilerArgs += COMPILATION_FLAGS
            }
        }
    }

    companion object {

        private val COMPILATION_FLAGS = listOf(
            "-Xjsr305=strict",
            "-Xopt-in=kotlin.ExperimentalStdlibApi",
            "-Xopt-in=kotlinx.serialization.ExperimentalSerializationApi"
        )
    }
}
