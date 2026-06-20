/*
 * HCsCR is a third-party mod for Minecraft Java Edition
 * that allows removing the end crystals faster.
 *
 * Copyright (c) 2023 Offenderify
 * Copyright (c) 2023-2026 VidTu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.vidtu.hcscr.compile;

import com.google.errorprone.annotations.CompileTimeConstant;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;

/**
 * A class that contains compile-time constants.
 * <p>
 * <b>Note:</b> This class is NEVER found in the final JAR. It <b>MUST NOT</b>
 * contain any references that are not inlined by the Java compiler.
 *
 * @author VidTu
 * @apiNote Internal use only
 * @see <a href="https://docs.oracle.com/javase/specs/jls/se8/html/jls-15.html#jls-15.28">Compile-time References</a>
 * @see Variables
 */
@ApiStatus.Internal
@NullMarked
public final class Constants {
    /**
     * URL for fetching the update info.
     */
    @CompileTimeConstant
    public static final String UPDATER_URL = "https://raw.githubusercontent.com/VidTu/HCsCR/main/updater_hcscr_fabric.properties";

    /**
     * Fallback updater link.
     */
    @CompileTimeConstant
    public static final String UPDATER_FALLBACK_LINK = "https://github.com/VidTu/HCsCR/releases/latest";

    /**
     * Connection/request timeout for the updater in seconds.
     * <p>
     * Equals to {@code 30} seconds.
     */
    @CompileTimeConstant
    public static final long UPDATER_TIMEOUT_SECONDS = 30L;

    /**
     * Maximum length for the updater response to prevent abuse.
     * <p>
     * Equals to {@code 32767} units.
     * <p>
     * Depending on the implementation, this might be counted
     * in either or both UTF-8 codepoints or UTF-8 bytes.
     */
    @CompileTimeConstant
    public static final int UPDATER_MAX_BODY_LENGTH = 32767;

    /**
     * Maximum length for the updater single component to prevent abuse.
     * <p>
     * Equals to {@code 255} units.
     * <p>
     * Depending on the implementation, this might be counted
     * in either or both UTF-8 codepoints or UTF-8 bytes.
     */
    @CompileTimeConstant
    public static final int UPDATER_MAX_COMPONENT_LENGTH = 255;

    /**
     * Magic separator character for property keys.
     * <p>
     * Equals to {@code '@'}.
     */
    @CompileTimeConstant
    public static final char UPDATER_KEY_SEPARATOR = '@';

    /**
     * Magic suffix for property keys indicating the displayed version.
     * <p>
     * Equals to {@code "version"}.
     */
    @CompileTimeConstant
    public static final String UPDATER_KEY_VERSION_SUFFIX = "version";

    /**
     * Magic suffix for property keys indicating the download link.
     * <p>
     * Equals to {@code "link"}.
     */
    @CompileTimeConstant
    public static final String UPDATER_KEY_LINK_SUFFIX = "link";

    /**
     * An instance of this class cannot be created.
     *
     * @throws AssertionError Always
     * @deprecated Always throws
     */
    @ApiStatus.ScheduledForRemoval
    @Deprecated
    @Contract(value = "-> fail", pure = true)
    private Constants() {
        throw new AssertionError("HCsCR: Compile-time code.");
    }
}
