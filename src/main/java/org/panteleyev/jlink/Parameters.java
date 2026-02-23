// Copyright © 2025-2026 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.jlink;

import org.gradle.api.GradleException;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.logging.Logger;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class Parameters {
    private final List<String> params = new ArrayList<>(60);
    private final Logger logger;

    public Parameters(Logger logger) {
        this.logger = logger;
    }

    public List<String> getParams() {
        return params;
    }

    public void add(String value) {
        logger.info("  {}", value);
        params.add(value);
    }

    public void addString(CommandLineParameter parameter, String value) {
        if (value == null || value.isEmpty()) return;

        logger.info("  {} {}", parameter.getName(), value);
        params.add(parameter.getName());
        params.add(value);
    }

    public void addBoolean(CommandLineParameter parameter, Property<Boolean> prop) {
        if (!prop.getOrElse(false)) return;

        logger.info("  {}", parameter.getName());
        params.add(parameter.getName());
    }

    public void addFile(CommandLineParameter parameter, DirectoryProperty prop, boolean mustExist) {
        if (!prop.isPresent()) return;

        var value = prop.get();

        var file = value.getAsFile();
        if (mustExist && !file.exists()) {
            throw new GradleException("File or directory " + file.getAbsolutePath() + " does not exist");
        }

        logger.info("  {} {}", parameter.getName(), file.getAbsolutePath());
        params.add(parameter.getName());
        params.add(file.getAbsolutePath());
    }

    public void addList(CommandLineParameter parameter, ListProperty<String> prop) {
        var list = prop.getOrElse(Collections.emptyList());
        if (!list.isEmpty()) {
            addString(parameter, String.join(",", list));
        }
    }

    public void addEnum(CommandLineParameter parameter, Property<? extends EnumParameter> prop) {
        if (!prop.isPresent()) return;
        addString(parameter, prop.get().getValue());
    }
}
