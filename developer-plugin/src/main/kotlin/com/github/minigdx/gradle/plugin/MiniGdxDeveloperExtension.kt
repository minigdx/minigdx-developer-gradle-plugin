package com.github.minigdx.gradle.plugin

import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectList
import org.gradle.api.Project
import org.gradle.api.provider.Property
import javax.inject.Inject

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

    val licence: Licence = project.objects.newInstance(Licence::class.java, project)

    fun licence(configuration: Action<Licence>) {
        configuration.execute(licence)
    }

    open class Licence @Inject constructor(project: Project) {
        /**
         * Name of the Licence.
         */
        val name: Property<String> = project.property()

        /**
         * URL of the Licence.
         */
        val url: Property<String> = project.property()
    }


    open class Developer @Inject constructor( project: Project) {

        val name: Property<String> = project.property()

        val email: Property<String> = project.property()

        val url: Property<String> = project.property()
    }

    val developers: NamedDomainObjectList<Developer> = project.objects.namedDomainObjectList(Developer::class.java)

    /**
     * Add a developer to this project.
     */
    fun developer(configure: Action<Developer>) {
        val developer = Developer(project)
        configure.execute(developer)
        developers.add(developer)
    }

    companion object {

        val IOS_MPP_PROPERTY = "minigdx.mpp.ios"
        val WASM_MPP_PROPERTY = "minigdx.mpp.wasm"
        val K2_MPP_PROPERTY = "minigdx.mpp.k2"
        val KTLINT_PROPERTY = "mnigdx.ktlint"
    }
}

private inline fun <reified T> Project.property(): Property<T> {
    return objects.property(T::class.java)
}
