// Copyright © 2024-2026 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.jlink;

enum CommandLineParameter {
    ADD_MODULES("--add-modules"),
    BIND_SERVICES("--bind-services"),
    ENDIAN("--endian"),
    GENERATE_CDS_ARCHIVE("--generate-cds-archive"),
    IGNORE_SIGNING_INFORMATION("--ignore-signing-information"),
    INCLUDE_LOCALES("--include-locales"),
    LIMIT_MODULES("--limit-modules"),
    MODULE_PATH("--module-path"),
    NO_HEADER_FILES("--no-header-files"),
    NO_MAN_PAGES("--no-man-pages"),
    OUTPUT("--output"),
    STRIP_DEBUG("--strip-debug"),
    VERBOSE("--verbose");

    private final String name;

    CommandLineParameter(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
