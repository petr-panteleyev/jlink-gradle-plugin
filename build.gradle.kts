// Copyright © 2025-2026 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
group = "org.panteleyev"
version = "2.0.0"

plugins {
    id("com.gradle.plugin-publish") version "2.0.0"
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(junit.jupiter)
    testRuntimeOnly(junit.platform.launcher)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

gradlePlugin {
    website = "https://github.com/petr-panteleyev/jlink-gradle-plugin"
    vcsUrl = "https://github.com/petr-panteleyev/jlink-gradle-plugin.git"
    plugins {
        register("jlinkplugin"){
            id = "org.panteleyev.jlinkplugin"
            version = project.version
            displayName = "JLink Gradle Plugin"
            description = "A plugin that executes jlink tool from JDK"
            implementationClass = "org.panteleyev.jlink.JLinkGradlePlugin"
            tags = listOf("jlink")
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
