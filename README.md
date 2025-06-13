# JLink Gradle Plugin

Gradle plugin for jlink.

[![Gradle Plugin Portal](https://img.shields.io/maven-metadata/v/https/plugins.gradle.org/m2/org/panteleyev/jlinkplugin/org.panteleyev.jlinkplugin.gradle.plugin/maven-metadata.xml.svg?label=Gradle%20Plugin)](https://plugins.gradle.org/plugin/org.panteleyev.jpackageplugin)
[![Gradle](https://img.shields.io/badge/Gradle-7.4%2B-green)](https://gradle.org/)
[![Java](https://img.shields.io/badge/Java-8-orange?logo=java)](https://www.oracle.com/java/technologies/javase-downloads.html)
[![GitHub](https://img.shields.io/github/license/petr-panteleyev/jlink-gradle-plugin)](LICENSE)

## Finding jlink

Plugin searches for ```jlink``` executable using the following priority list:

1. Configured toolchain

2. ```java.home``` system property.

Though rarely required it is possible to override toolchain for particular ```jlink``` task:

```kotlin
javaLauncher = javaToolchains.launcherFor {
    languageVersion = JavaLanguageVersion.of(21)
}
```

## Configuration

### Parameters

| Parameter                | Type                       | jlink argument               |
|--------------------------|----------------------------|------------------------------|
| addModules               | ListProperty&lt;String>    | --add-modules                |
| bindServices             | Property&lt;Boolean>       | --bind-services              |
| endian                   | Property&lt;Endian>        | --endian                     |
| generateCdsArchive       | Property&lt;Boolean>       | --generate-cds-archive       |
| ignoreSigningInformation | Property&lt;Boolean>       | --ignore-signing-information |
| includeLocales           | ListProperty&lt;String>    | --include-locales            |
| limitModules             | ListProperty&lt;String>    | --limit-modules              |
| modulePaths              | ConfigurableFileCollection | --module-path                |
| noHeaderFiles            | Property&lt;Boolean>       | --no-header-files            |
| noManPages               | Property&lt;Boolean>       | --no-man-pages               |
| output                   | DirectoryProperty          | --output                     |
| stripDebug               | Property&lt;Boolean>       | --strip-debug                |
| verbose                  | Property&lt;Boolean>       | --verbose                    |

### Endian

| Plugin Value | jlink Value |
|--------------|-------------|
| LITTLE       | little      |
| BIG          | big         |

### Output Directory

```jlink``` utility fails if output directory already exists. At the same time gradle always creates plugin output
directory.

In order to work around this behaviour plugin always tries to delete directory specified by ```output``` before
launching ```jlink```.

For safety reasons ```output``` must point to the location inside ```${layout.buildDirectory}```.

## Logging

Plugin uses ```LogLevel.INFO``` to print various information about toolchain, ```jlink``` parameters, etc. Use gradle
option ```--info``` to check this output.

## Dry Run Mode

To execute plugin tasks in dry run mode without calling ```jlink``` set property```jlink.dryRun``` to true.

_Example:_

```shell
$ ./gradlew clean build jlink --info -Djlink.dryRun=true
```

## Configuration Cache

This plugin should be compatible with
Gradle [configuration cache](https://docs.gradle.org/current/userguide/configuration_cache.html).

## Configuration Example

```kotlin
tasks.register("copyDependencies", Copy::class) {
    from(configurations.runtimeClasspath).into(layout.buildDirectory.dir("jmods"))
}

tasks.register("copyJar", Copy::class) {
    from(tasks.jar).into(layout.buildDirectory.dir("jmods"))
}

tasks.jlink {
    dependsOn("build", "copyDependencies", "copyJar")

    modulePaths.setFrom(tasks.named("copyJar"))
    addModules = listOf("ALL-MODULE-PATH")

    noHeaderFiles = true
    noManPages = true
    stripDebug = true
    generateCdsArchive = true

    output.set(layout.buildDirectory.dir("jlink"))
}

```

## References

[The jlink Command](https://docs.oracle.com/en/java/javase/24/docs/specs/man/jlink.html)
