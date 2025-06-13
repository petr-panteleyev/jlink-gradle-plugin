/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.jlink;

import static org.gradle.internal.os.OperatingSystem.current;

final class OsUtil {
    static boolean isWindows() {
        return current().isWindows();
    }

    static boolean isMac() {
        return current().isMacOsX();
    }

    static boolean isLinux() {
        return current().isLinux();
    }

    private OsUtil() {
    }
}
