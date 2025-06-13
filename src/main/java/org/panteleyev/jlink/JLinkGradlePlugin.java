/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.jlink;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class JLinkGradlePlugin implements Plugin<Project> {
    private static final String GROUP = "Distribution";
    private static final String DESCRIPTION = "Creates Java runtime bundle using jlink.";

    @Override
    public void apply(Project target) {
        target.getTasks().register("jlink", JLinkTask.class, task -> {
            task.setGroup(GROUP);
            task.setDescription(DESCRIPTION);
        });
    }
}
