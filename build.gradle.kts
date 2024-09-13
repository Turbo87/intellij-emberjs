import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    // Java support
    id("java")
    // Kotlin support
    id("org.jetbrains.kotlin.jvm") version "2.0.20"
    // gradle-intellij-plugin - read more: https://github.com/JetBrains/gradle-intellij-plugin
    id("org.jetbrains.intellij.platform") version "2.0.1"
    // id("org.jetbrains.intellij.platform.migration") version "2.0.1"
}

group = "com.emberjs"
version = "2024.1.1"

// Configure project's dependencies
repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}
dependencies {
    intellijPlatform {
        create("IU", "2024.2")
        plugins(listOf("com.dmarcotte.handlebars:242.20224.159"))
        bundledPlugins(listOf("JavaScript", "com.intellij.css", "org.jetbrains.plugins.yaml"))
        instrumentationTools()
        testFramework(TestFrameworkType.Platform)
    }
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.assertj:assertj-core:3.26.3")
    testImplementation("org.junit.platform:junit-platform-launcher:1.11.0")
    implementation(kotlin("test"))
    implementation("org.codehaus.jettison:jettison:1.5.4")
}

intellijPlatform {
    pluginConfiguration {
        name.set("Ember.js")
    }
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    publishPlugin {
        token.set(System.getenv("ORG_GRADLE_PROJECT_intellijPublishToken"))
    }
}
