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
     * Minimum amount of nanoseconds the entity removal can be delayed for. (after hitting)
     * <p>
     * Equals to {@code 0} nanos.
     */
    @CompileTimeConstant
    public static final int MIN_CRYSTALS_DELAY = 0;

    /**
     * Default value for the amount of nanoseconds the entity removal can be delayed for. (after hitting)
     * <p>
     * Equals to {@link #MIN_CRYSTALS_DELAY}. ({@code 0} nanos)
     */
    @CompileTimeConstant
    public static final int DEFAULT_CRYSTALS_DELAY = MIN_CRYSTALS_DELAY;

    /**
     * Maximum amount of nanoseconds the entity removal can be delayed for. (after hitting)
     * <p>
     * Equals to {@code 200_000_000} nanos. ({@code 200} ms)
     */
    @CompileTimeConstant
    public static final int MAX_CRYSTALS_DELAY = 200_000_000;

    /**
     * The precision/resolution/rounding of the crystal delay setting.
     * <p>
     * Equals to {@code 1_000_000} nanos. (rounds nanoseconds to milliseconds)
     */
    @CompileTimeConstant
    public static final int CRYSTALS_DELAY_RESOLUTION = 1_000_000;

    /**
     * Minimum amount of ticks the entities are allowed to be hidden for. (before resync)
     * <p>
     * Equals to {@code 0} ticks.
     */
    @CompileTimeConstant
    public static final int MIN_CRYSTALS_RESYNC = 0;

    /**
     * Default value for the amount of ticks the entities are allowed to be hidden for. (before resync)
     * <p>
     * Equals to {@code 20} ticks. (i.e., {@code 1} second)
     */
    @CompileTimeConstant
    public static final int DEFAULT_CRYSTALS_RESYNC = 20;

    /**
     * Maximum amount of ticks the entities are allowed to be hidden for. (before resync)
     * <p>
     * Equals to {@code 50} ticks. (i.e., {@code 2.5} seconds)
     */
    @CompileTimeConstant
    public static final int MAX_CRYSTALS_RESYNC = 50;

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
