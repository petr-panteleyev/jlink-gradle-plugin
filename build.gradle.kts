/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
group = "org.panteleyev"
version = "1.0.0"

plugins {
    java
    `java-gradle-plugin`
    `maven-publish`
    id("com.gradle.plugin-publish") version "1.3.1"
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.13.1")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

gradlePlugin {
    val jlink by plugins.creating {
        id = "org.panteleyev.jlinkplugin"
        version = project.version
        displayName = "JLink Gradle Plugin"
        description = "A plugin that executes jlink tool from JDK"
        implementationClass = "org.panteleyev.jlink.JLinkGradlePlugin"
    }
}

pluginBundle {
    website = "https://github.com/petr-panteleyev/jlink-gradle-plugin"
    vcsUrl = "https://github.com/petr-panteleyev/jlink-gradle-plugin.git"
    tags = listOf("jlink")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
