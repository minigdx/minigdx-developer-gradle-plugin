package com.github.minigdx.gradle.plugin

import org.gradle.api.Project
import org.gradle.api.provider.Property

open class MiniGdxDeveloperExtension(private val project: Project) {

    /**
     * Name of the project.
     */
    val name: Property<String> = project.property()

    /**
     * Short description of what the project is.
     */
    val description: Property<String> = project.property()

    /**
     * Project url WITHOUT trailing "/"
     */
    val projectUrl: Property<String> = project.property()

    val licence = Licence(project)

    fun licence(configuration: Licence.() -> Unit) {
        configuration(licence)
    }

    class Licence(project: Project) {
        /**
         * Name of the Licence.
         */
        val name: Property<String> = project.property()

        /**
         * URL of the Licence.
         */
        val url: Property<String> = project.property()
    }

    class Developer(project: Project) {

        val name: Property<String> = project.property()

        val email: Property<String> = project.property()

        val url: Property<String> = project.property()
    }

    val developers = mutableListOf<Developer>()

    /**
     * Add a developer to this project.
     */
    fun developer(configure: Developer.() -> Unit) {
        val developer = Developer(project)
        configure(developer)
        developers.add(developer)
    }

    companion object {

        val IOS_MPP_PROPERTY = "minigdx.mpp.ios"
    }
}

private inline fun <reified T> Project.property(): Property<T> {
    return objects.property(T::class.java)
}
