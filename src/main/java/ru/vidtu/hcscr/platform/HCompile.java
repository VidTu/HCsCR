/*
 * HCsCR is a third-party mod for Minecraft Java Edition
 * that allows removing the end crystals faster.
 *
 * Copyright (c) 2023 Offenderify
 * Copyright (c) 2023-2025 VidTu
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

package ru.vidtu.hcscr.platform;

import com.google.errorprone.annotations.CompileTimeConstant;
import name.remal.gradle_plugins.build_time_constants.api.BuildTimeConstants;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;

/**
 * A class that contains compile-time boolean flags.
 *
 * @author VidTu
 * @apiNote Internal use only
 */
@ApiStatus.Internal
@NullMarked
public final class HCompile {
    /**
     * Whether the debug features are enabled.
     */
    @CompileTimeConstant
    private static final boolean DEBUG = BuildTimeConstants.getBooleanProperty("debug");

    /**
     * Whether the additional Java assertions are enabled.
     */
    @CompileTimeConstant
    public static final boolean DEBUG_ASSERTS = DEBUG;

    /**
     * Whether the {@code DEBUG} and {@code TRACE} logs are generated.
     */
    @CompileTimeConstant
    public static final boolean DEBUG_LOGS = DEBUG;

    /**
     * Whether the Minecraft's {@link ProfilerFiller} class is used.
     */
    @CompileTimeConstant
    public static final boolean DEBUG_PROFILER = DEBUG;

    /**
     * An instance of this class cannot be created.
     *
     * @throws AssertionError Always
     * @deprecated Always throws
     */
    @ApiStatus.ScheduledForRemoval
    @Deprecated
    @Contract(value = "-> fail", pure = true)
    private HCompile() {
        throw new AssertionError("HCsCR: No instances.");
    }
}
