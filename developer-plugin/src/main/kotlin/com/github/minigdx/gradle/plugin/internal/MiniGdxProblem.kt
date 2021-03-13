package com.github.minigdx.gradle.plugin.internal

import me.champeau.jdoctor.BaseProblem
import me.champeau.jdoctor.render.SimpleTextRenderer
import org.gradle.api.GradleException
import org.gradle.api.Project
import java.util.Optional
import java.util.function.Supplier
import me.champeau.jdoctor.Solution as JSolution

class MiniGdxProblem(
    severity: Severity,
    context: Context,
    shortDescription: Supplier<String?>,
    reason: Supplier<String>,
    docUrl: Supplier<String?>,
    solutions: List<Supplier<JSolution>>
) : BaseProblem<MiniGdxProblem.ProblemId, Severity, Context, MiniGdxProblem.Payload>(
    ProblemId.PROBLEM,
    severity,
    context,
    Payload(),
    shortDescription,
    Supplier { -> null },
    reason,
    docUrl,
    solutions
) {

    enum class ProblemId {
        PROBLEM
    }

    class Payload
}

enum class Severity {
    /**
     * Easy fixed issues like configuration issue.
     */
    EASY,

    /**
     * Might be annoying to fix for average gradle users
     */
    AVERAGE,

    /**
     * Bug in the plugin, can't be fixed
     */
    GRAVE
}

class Solution(val description: String, val documentedAt: String? = null) : JSolution {

    override fun getShortDescription(): String {
        return if (documentedAt == null) {
            description
        } else {
            "$description\n" +
                "Documented at: $documentedAt"
        }
    }

    override fun getDocumentationLink(): Optional<String> {
        return Optional.ofNullable(documentedAt)
    }
}

data class Context(val project: Project, val severity: Severity) {

    override fun toString(): String {
        return when (severity) {
            Severity.EASY,
            Severity.AVERAGE -> "Gradle module '${project.name}'"
            Severity.GRAVE -> """Gradle module '${project.name}'
                
                OS name: '${System.getProperty("os.name")}'
                JVM version: '${System.getProperty("java.version")}'
                JVM name: '${System.getProperty("java.vendor")}'
                Gradle version: '${project.gradle.gradleVersion}'
                Plugin version: '${this::class.java.getPackage().implementationVersion}' 
                """
        }
    }
}

class MiniGdxException(
    message: String,
    cause: Throwable? = null
) : GradleException(message, cause) {

    companion object {

        fun create(
            severity: Severity,
            project: Project,
            because: String,
            description: String?,
            documentedAt: String? = null,
            solutions: List<Solution> = emptyList(),
            cause: Throwable? = null
        ): MiniGdxException {
            val problem = MiniGdxProblem(
                severity,
                Context(project, severity),
                { description },
                { because },
                { documentedAt },
                solutions.map { Supplier { it } }
            )

            val message = SimpleTextRenderer.render(problem)
            return MiniGdxException(message, cause)
        }

        const val ISSUES = "https://github.com/minigdx/minigdx-gradle-plugin/issues"
    }
}
