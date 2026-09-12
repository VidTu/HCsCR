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

package ru.vidtu.hcscr.platform;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import ru.vidtu.hcscr.HCsCR;
import ru.vidtu.hcscr.compile.Variables;
import ru.vidtu.hcscr.config.ConfigScreen;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 * A helper class that contains methods that depend on Stonecutter, a Java source code preprocessor.
 *
 * @author VidTu
 * @apiNote Internal use only
 * @deprecated Centralized preprocessing is bad design
 */
@ApiStatus.Internal
@Deprecated
@NullMarked
public final class HStonecutter {
    /**
     * An instance of this class cannot be created.
     *
     * @throws AssertionError Always
     * @deprecated Always throws
     */
    @ApiStatus.ScheduledForRemoval
    @Deprecated
    @Contract(value = "-> fail", pure = true)
    private HStonecutter() {
        if (Variables.DEBUG_ASSERTS) {
            throw new AssertionError("HCsCR: No instances.");
        }
    }

    /**
     * Creates a new translatable component.
     *
     * @param key Translation key
     * @return A new translatable component
     */
    @Contract(value = "_ -> new", pure = true)
    public static MutableComponent translate(final String key) {
        // Validate.
        if (Variables.DEBUG_ASSERTS) {
            assert (key != null) : "HCsCR: Parameter 'key' is null.";
            assert (!key.isEmpty()) : "HCsCR: Creating a translatable component with an empty key.";
        }

        // Delegate.
        //? if >=1.19.2 {
        return Component.translatable(key);
        //?} else {
        /*return new net.minecraft.network.chat.TranslatableComponent(key);
        *///?}
    }

    /**
     * Creates a new translatable component.
     *
     * @param key  Translation key
     * @param args Translation args
     * @return A new translatable component
     */
    @Contract(value = "_, _ -> new", pure = true)
    public static MutableComponent translate(final String key, final Object... args) {
        // Validate.
        if (Variables.DEBUG_ASSERTS) {
            assert (key != null) : "HCsCR: Parameter 'key' is null. (args: " + Arrays.toString(args) + ')';
            assert (args != null) : "HCsCR: Parameter 'args' is null. (key: " + key + ')';
            assert (!key.isEmpty()) : "HCsCR: Creating a translatable component with an empty key. (args: " + Arrays.toString(args) + ')';
            assert (args.length != 0) : "HCsCR: Creating a translatable components with empty args array. (key: " + key + ')';
        }

        // Delegate.
        //? if >=1.19.2 {
        return Component.translatable(key, args);
        //?} else {
        /*return new net.minecraft.network.chat.TranslatableComponent(key, args);
        *///?}
    }

    /**
     * Gets the profiler of the game client.
     *
     * @param client Client game instance
     * @return Client profiler
     */
    @Contract(pure = true)
    public static ProfilerFiller profilerOfClient(final Minecraft client) {
        // Validate.
        if (Variables.DEBUG_ASSERTS) {
            assert (client != null) : "HCsCR: Parameter 'client' is null.";
            assert (client.isSameThread()) : "HCsCR: Getting the client profiler NOT from the main thread. (thread: " + Thread.currentThread() + ", client: " + client + ')';
        }

        // Throw unconditionally.
        if (!Variables.DEBUG_PROFILER) {
            throw (Variables.DEBUG_ASSERTS ? new AssertionError("HCsCR: This mod build hasn't been compiled with profiler support.") : null);
        }

        // Delegate.
        //? if >=1.21.3 {
        return net.minecraft.util.profiling.Profiler.get();
        //?} else {
        /*return client.getProfiler(); // Implicit NPE for 'client'
        *///?}
    }
}
