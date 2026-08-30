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

//? if >=1.21.11 {
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.resources.Identifier;
import net.minecraft.util.profiling.Profiler;
//?} elif >=1.21.8 {
/*import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.profiling.Profiler;
*///?} elif >=1.21.4 {
/*import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.profiling.Profiler;
*///?} elif >=1.21.3 {
/*import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.profiling.Profiler;
*///?} elif >=1.20.6 {
/*import net.minecraft.resources.ResourceLocation;
*///?} elif >=1.19.4 {
/*import net.minecraft.resources.ResourceLocation;
*///?} elif >=1.19.2 {
/*import net.minecraft.resources.ResourceLocation;
*///?} elif >=1.17.1 {
/*import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
*///?} else {
/*import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
*///?}

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
     * A channel identifier for servers to know that this mod is installed.
     */
    //? if >=1.21.11 {
    /*package-private*/ static final Identifier CHANNEL_IDENTIFIER = Identifier.fromNamespaceAndPath("hcscr", "imhere");
    //?} elif >=1.21.1 || (forge && (!hacky_neoforge) && >=1.18.2 && (!1.20.2)) {
    /*/^package-private^/ static final ResourceLocation CHANNEL_IDENTIFIER = ResourceLocation.fromNamespaceAndPath("hcscr", "imhere");
    *///?} else {
    /*/^package-private^/ static final ResourceLocation CHANNEL_IDENTIFIER = new ResourceLocation("hcscr", "imhere");
    *///?}

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
        /*return new TranslatableComponent(key);
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
        /*return new TranslatableComponent(key, args);
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
        return Profiler.get();
        //?} else {
        /*return client.getProfiler(); // Implicit NPE for 'client'
        *///?}
    }

    /**
     * Gets the level of the entity.
     *
     * @param entity Target entity to get the level of
     * @return The level (world) in which the entity is currently located or was last located
     */
    @Contract(pure = true)
    public static Level levelOfEntity(final Entity entity) {
        // Validate.
        if (Variables.DEBUG_ASSERTS) {
            assert (entity != null) : "HCsCR: Parameter 'entity' is null.";
            // No thread checks here because this can be called from the integrated server.
        }

        //? if >=1.20.1 {
        return entity.level(); // Implicit NPE for 'entity'
        //?} else {
        /*return entity.level; // Implicit NPE for 'entity'
        *///?}
    }

    /**
     * Checks whether the entity has been removed from the world or marked for removal from the world.
     *
     * @param entity Target entity to check
     * @return Whether the entity has been removed
     * @see #removeEntity(Entity)
     */
    @SuppressWarnings({"deprecation", "RedundantSuppression"}) // <- Forge 1.16.5.
    @Contract(pure = true)
    public static boolean isEntityRemoved(final Entity entity) {
        // Validate.
        if (Variables.DEBUG_ASSERTS) {
            assert (entity != null) : "HCsCR: Parameter 'entity' is null.";
            assert (Minecraft.getInstance().isSameThread()) : "HCsCR: Checking entity removal NOT from the main thread. (thread: " + Thread.currentThread() + ", entity: " + entity + ')';
        }

        // Delegate.
        //? if >=1.17.1 {
        return entity.isRemoved(); // Implicit NPE for 'entity'
        //?} else {
        /*return entity.removed; // Implicit NPE for 'entity'
        *///?}
    }
}
