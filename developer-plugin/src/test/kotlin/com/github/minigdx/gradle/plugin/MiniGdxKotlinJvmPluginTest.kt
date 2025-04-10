package com.github.minigdx.gradle.plugin

import org.gradle.testfixtures.ProjectBuilder
import org.jetbrains.kotlin.gradle.plugin.extraProperties
import kotlin.test.Test
import kotlin.test.assertNotNull

class MiniGdxKotlinJvmPluginTest {
    @Test
    fun `plugin registers task`() {
        // Create a test project and apply the plugin
        val project = ProjectBuilder.builder().build().also {
            it.extraProperties.set("minigdx.dokka.skip", "true")
        }
        project.plugins.apply("com.github.minigdx.gradle.plugin.developer.jvm")

        // Verify the result
        assertNotNull(project.tasks.findByName("createGithubWorkflows"))
        assertNotNull(project.tasks.findByName("createMakefile"))
        assertNotNull(project.tasks.findByName("compileKotlin"))
    }
}
