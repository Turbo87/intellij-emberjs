import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    // Java support
    id("java")
    // Kotlin support
    id("org.jetbrains.kotlin.jvm") version "2.2.21"
    // gradle-intellij-plugin - read more: https://github.com/JetBrains/gradle-intellij-plugin
    // id("org.jetbrains.intellij.platform") version "2.10.2"
    id("org.jetbrains.intellij.platform") version "2.10.1"
}

group = "com.emberjs"
version = "2024.3.1"

// Configure project's dependencies
repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}
dependencies {
    intellijPlatform {
        create("IU", "2024.3.6")
        plugins(listOf("com.dmarcotte.handlebars:243.21565.122"))
        bundledPlugins(listOf("JavaScript", "com.intellij.css", "org.jetbrains.plugins.yaml"))
        testFramework(TestFrameworkType.Platform)
    }
    implementation("org.codehaus.jettison:jettison:1.5.4")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.assertj:assertj-core:3.27.6")
    testImplementation("org.junit.platform:junit-platform-launcher:1.11.0")
    implementation(kotlin("test"))
}

// Configure gradle-intellij-plugin plugin.
// Read more: https://github.com/JetBrains/gradle-intellij-plugin
intellijPlatform {
    pluginConfiguration {
        name.set("Ember.js")
    }

    sandboxContainer.set(project.rootDir.resolve(".sandbox"))
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "21"
        targetCompatibility = "21"
    }
    withType<KotlinCompile> {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }

    publishPlugin {
        token.set(System.getenv("ORG_GRADLE_PROJECT_intellijPublishToken"))
    }
}
