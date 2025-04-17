import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.gradle.publish)
    // Apply the Java Gradle plugin development plugin to add support for developing Gradle plugins
    `java-gradle-plugin`

    `maven-publish`

    alias(libs.plugins.kotlin.jvm)
}

group = "com.github.minigdx"
version = project.properties["version"] ?: "DEV-SNAPSHOT"
if(version == "unspecified") {
    version = "DEV-SNAPSHOT"
}
repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    // Align versions of all Kotlin components
    implementation(platform(libs.kotlin.bom))

    // Use the Kotlin JDK 8 standard library.
    implementation(libs.kotlin.stdlib)

    implementation(libs.kotlin.plugin.mpp)
    implementation(libs.kotlin.plugin.dokka)
    implementation(libs.ktlint.plugin)

    implementation(platform(libs.jdoctor.bom))
    implementation(libs.jdoctor.core)
    implementation(libs.jdoctor.utils)

    implementation(libs.publish.on.central.plugin)

    // Use the Kotlin test library.
    testImplementation(libs.bundles.test)
}

gradlePlugin {
    website.set("https://github.com/minigdx/minigdx-developer-gradle-plugin")
    vcsUrl.set("https://github.com/minigdx/minigdx-developer-gradle-plugin")

    // Define the plugin
    val developer by plugins.creating {
        id = "com.github.minigdx.gradle.plugin.developer"
        implementationClass = "com.github.minigdx.gradle.plugin.MiniGdxDeveloperPlugin"
        displayName = "MiniGDX Developer plugin"
        description = """Configure MiniGDX libs with a common set of configuration and tasks.
                | The usage is mainly for MiniGDX contributors.
            """.trimMargin()
        tags.set(listOf("minigdx", "developer", "kotlin", "jvm", "mpp", "ios", "js", "android", "native"))
    }

    val mpp by plugins.creating {
        id = "com.github.minigdx.gradle.plugin.developer.mpp"
        implementationClass = "com.github.minigdx.gradle.plugin.MiniGdxKotlinMppPlugin"
        displayName = "MiniGDX Kotlin JVM Developer plugin"
        description = """Configure MiniGDX libs to build for the JVM only.
                | The usage is mainly for MiniGDX contributors.
            """.trimMargin()
        tags.set(listOf("minigdx", "developer", "kotlin", "jvm", "mpp", "ios", "js", "android", "native"))
    }

    val jvm by plugins.creating {
        id = "com.github.minigdx.gradle.plugin.developer.jvm"
        implementationClass = "com.github.minigdx.gradle.plugin.MiniGdxKotlinJvmPlugin"
        displayName = "MiniGDX Kotlin Multiplatform Developer plugin"
        description = """Configure MiniGDX libs to build for different platforms.
                | The usage is mainly for MiniGDX contributors.
            """.trimMargin()
        tags.set(listOf("minigdx", "developer", "kotlin", "jvm"))
    }
}


// Add a source set for the functional test suite
val functionalTestSourceSet = sourceSets.create("functionalTest") {}

gradlePlugin.testSourceSets(functionalTestSourceSet)
configurations["functionalTestImplementation"].extendsFrom(configurations["testImplementation"])

// Add a task to run the functional tests
val functionalTest by tasks.registering(Test::class) {
    testClassesDirs = functionalTestSourceSet.output.classesDirs
    classpath = functionalTestSourceSet.runtimeClasspath
}

tasks.check {
    // Run the functional tests as part of `check`
    dependsOn(functionalTest)
}

val javaVersion = JavaLanguageVersion.of(17)
java {
    toolchain {
        languageVersion.set(javaVersion)
    }
}

kotlin {
    jvmToolchain {
        this.languageVersion.set(javaVersion)
    }
}

project.tasks.withType(KotlinCompile::class.java).configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
    val toolchainService = project.extensions.getByType(JavaToolchainService::class.java)
    kotlinJavaToolchain.toolchain.use(
        toolchainService.launcherFor {
            languageVersion.set(javaVersion)
        }
    )
}

// Ensure "org.gradle.jvm.version" is set to "17" in Gradle metadata.
project.tasks.withType(JavaCompile::class.java).configureEach {
    sourceCompatibility = JavaVersion.VERSION_17.toString()
    targetCompatibility = JavaVersion.VERSION_17.toString()
}

testing.suites {
    withType<JvmTestSuite>().configureEach {
        useJUnitJupiter()
        dependencies {
            implementation(gradleTestKit())
        }
    }
}