// Copyright © 2025-2026 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.jlink;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.jvm.toolchain.JavaLauncher;
import org.gradle.jvm.toolchain.JavaToolchainService;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Optional;

import static org.panteleyev.jlink.CommandLineParameter.ADD_MODULES;
import static org.panteleyev.jlink.CommandLineParameter.BIND_SERVICES;
import static org.panteleyev.jlink.CommandLineParameter.ENDIAN;
import static org.panteleyev.jlink.CommandLineParameter.GENERATE_CDS_ARCHIVE;
import static org.panteleyev.jlink.CommandLineParameter.IGNORE_SIGNING_INFORMATION;
import static org.panteleyev.jlink.CommandLineParameter.INCLUDE_LOCALES;
import static org.panteleyev.jlink.CommandLineParameter.LIMIT_MODULES;
import static org.panteleyev.jlink.CommandLineParameter.MODULE_PATH;
import static org.panteleyev.jlink.CommandLineParameter.NO_HEADER_FILES;
import static org.panteleyev.jlink.CommandLineParameter.NO_MAN_PAGES;
import static org.panteleyev.jlink.CommandLineParameter.OUTPUT;
import static org.panteleyev.jlink.CommandLineParameter.STRIP_DEBUG;
import static org.panteleyev.jlink.CommandLineParameter.VERBOSE;
import static org.panteleyev.jlink.DirectoryUtil.isNestedDirectory;
import static org.panteleyev.jlink.DirectoryUtil.removeDirectory;
import static org.panteleyev.jlink.OsUtil.isWindows;

public abstract class JLinkTask extends DefaultTask {
    static final String EXECUTABLE = "jlink";

    private final boolean dryRun;

    public JLinkTask() {
        dryRun = Boolean.getBoolean("jlink.dryRun");

        try {
            var toolchain = getProject().getExtensions()
                    .getByType(JavaPluginExtension.class).getToolchain();
            var defaultLauncher = getJavaToolchainService().launcherFor(toolchain);
            getJavaLauncher().convention(defaultLauncher);
        } catch (Exception ex) {
            getLogger().trace("Failed to configure JavaLauncher");
        }
    }

    @Inject
    public abstract JavaToolchainService getJavaToolchainService();

    @Inject
    public abstract ProjectLayout getProjectLayout();

    @Nested
    @org.gradle.api.tasks.Optional
    public abstract Property<JavaLauncher> getJavaLauncher();

    @Input
    @org.gradle.api.tasks.Optional
    public abstract ListProperty<String> getAddModules();

    @Input
    @org.gradle.api.tasks.Optional
    public abstract Property<Boolean> getBindServices();

    @Input
    @org.gradle.api.tasks.Optional
    public abstract Property<Endian> getEndian();

    @Input
    @org.gradle.api.tasks.Optional
    public abstract Property<Boolean> getGenerateCdsArchive();

    @Input
    @org.gradle.api.tasks.Optional
    public abstract Property<Boolean> getIgnoreSigningInformation();

    @Input
    @org.gradle.api.tasks.Optional
    public abstract ListProperty<String> getIncludeLocales();

    @Input
    @org.gradle.api.tasks.Optional
    public abstract ListProperty<String> getLimitModules();

    @InputFiles
    @org.gradle.api.tasks.Optional
    public abstract ConfigurableFileCollection getModulePaths();

    @Input
    @org.gradle.api.tasks.Optional
    public abstract Property<Boolean> getNoHeaderFiles();

    @Input
    @org.gradle.api.tasks.Optional
    public abstract Property<Boolean> getNoManPages();

    @Input
    @org.gradle.api.tasks.Optional
    public abstract Property<Boolean> getStripDebug();

    @Input
    @org.gradle.api.tasks.Optional
    public abstract Property<Boolean> getVerbose();

    @OutputDirectory
    public abstract DirectoryProperty getOutput();

    @TaskAction
    public void action() {
        if (dryRun) {
            getLogger().lifecycle("Executing jlink plugin in dry run mode");
        }

        var jlink = getJLinkFromToolchain()
                .orElseGet(() -> getJLinkFromJavaHome()
                        .orElseThrow(() -> new GradleException("Could not detect " + EXECUTABLE)));

        execute(jlink);
    }

    private Optional<String> buildExecutablePath(String home) {
        var executable = home + File.separator + "bin" + File.separator + EXECUTABLE + (isWindows() ? ".exe" : "");
        if (new File(executable).exists()) {
            return Optional.of(executable);
        } else {
            getLogger().warn("File {} does not exist", executable);
            return Optional.empty();
        }
    }

    private Optional<String> getJLinkFromToolchain() {
        getLogger().info("Looking for {} in toolchain", EXECUTABLE);
        try {
            var launcherValue = getJavaLauncher().getOrNull();
            if (launcherValue == null) {
                throw new RuntimeException();
            } else {
                var home = launcherValue.getMetadata().getInstallationPath().getAsFile().getAbsolutePath();
                getLogger().info("toolchain: {}", home);
                return buildExecutablePath(home);
            }
        } catch (Exception ex) {
            getLogger().info("Toolchain is not configured");
            return Optional.empty();
        }
    }

    private Optional<String> getJLinkFromJavaHome() {
        getLogger().info("Getting {} from java.home", EXECUTABLE);
        var javaHome = System.getProperty("java.home");
        if (javaHome == null) {
            getLogger().error("java.home is not set");
            return Optional.empty();
        }
        return buildExecutablePath(javaHome);
    }

    private void buildParameters(Parameters parameters) {
        parameters.addList(ADD_MODULES, getAddModules());
        parameters.addBoolean(BIND_SERVICES, getBindServices());
        parameters.addEnum(ENDIAN, getEndian());
        parameters.addBoolean(GENERATE_CDS_ARCHIVE, getGenerateCdsArchive());
        parameters.addBoolean(IGNORE_SIGNING_INFORMATION, getIgnoreSigningInformation());
        parameters.addList(INCLUDE_LOCALES, getIncludeLocales());
        parameters.addList(LIMIT_MODULES, getLimitModules());

        var mPaths = new ArrayList<String>();
        getModulePaths().forEach(f -> mPaths.add(f.getAbsolutePath()));
        parameters.addString(MODULE_PATH, String.join(File.pathSeparator, mPaths));

        parameters.addBoolean(NO_HEADER_FILES, getNoHeaderFiles());
        parameters.addBoolean(NO_MAN_PAGES, getNoManPages());
        parameters.addFile(OUTPUT, getOutput(), false);
        parameters.addBoolean(STRIP_DEBUG, getStripDebug());
        parameters.addBoolean(VERBOSE, getVerbose());
    }

    private void execute(String cmd) {
        var parameters = new Parameters(getLogger());
        parameters.add(cmd.contains(" ") ? "\"" + cmd + "\"" : cmd);
        buildParameters(parameters);

        if (dryRun) return;

        var outputPath = getOutput().getAsFile().get().toPath().toAbsolutePath();
        if (!isNestedDirectory(getProjectLayout().getBuildDirectory().getAsFile().get().toPath(), outputPath)) {
            getLogger().error("Cannot remove output folder, must belong to {}",
                    getProjectLayout().getBuildDirectory().get());
        } else {
            getLogger().warn("Trying to remove output {}", outputPath);
            removeDirectory(outputPath);
        }

        try {
            var processBuilder = new ProcessBuilder();

            var process = processBuilder
                    .redirectErrorStream(true)
                    .command(parameters.getParams())
                    .start();

            getLogger().info(EXECUTABLE + " output:");

            try (var reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    getLogger().info(line);
                }
            }

            int status = process.waitFor();
            if (status != 0) {
                throw new GradleException("Error while executing " + EXECUTABLE);
            }
        } catch (Exception ex) {
            throw new GradleException("Error while executing " + EXECUTABLE, ex);
        }
    }
}
