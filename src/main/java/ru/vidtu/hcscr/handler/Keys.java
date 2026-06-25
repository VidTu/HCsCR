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
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.vidtu.hcscr.handler;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.NullMarked;
import ru.vidtu.hcscr.HCsCR;
import ru.vidtu.hcscr.compile.Variables;
import ru.vidtu.hcscr.config.Config;
import ru.vidtu.hcscr.config.ConfigScreen;
import ru.vidtu.hcscr.platform.HStonecutter;

//? if >=1.21.11 {
import net.minecraft.resources.Identifier;
//?} else {
/*import net.minecraft.resources.ResourceLocation;
*///?}

/**
 * Handling logic for the mod's keys. (aka key-bindings aka key-mappings)
 *
 * @author VidTu
 * @apiNote Internal use only
 * @see #CONFIG
 * @see #TOGGLE
 */
@ApiStatus.Internal
@NullMarked
public final class Keys {
    //? if >=1.21.10 {
        //~ if >=1.21.11 'ResourceLocation' -> 'Identifier' {
            //~ if neoforge 'KeyMapping.Category.register' -> 'new KeyMapping.Category' {
    /**
     * Key category for {@link #CONFIG} and {@link #TOGGLE}.
     *
     * @see #CONFIG
     * @see #TOGGLE
     */
    public static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("hcscr", "root"));
            //~}
        //~}
    //?}

    /**
     * "Open the config screen" key. Not bound by default.
     *
     * @see #config(Minecraft, ProfilerFiller)
     */
    //~ if >=1.21.10 '"key.category.hcscr.root"' -> 'CATEGORY' {
    public static final KeyMapping CONFIG = new KeyMapping("hcscr.key.config", InputConstants.UNKNOWN.getValue(), CATEGORY);

    /**
     * "Toggle the mod" key. Not bound by default.
     *
     * @see #toggle(Minecraft, ProfilerFiller)
     */
    public static final KeyMapping TOGGLE = new KeyMapping("hcscr.key.toggle", InputConstants.UNKNOWN.getValue(), CATEGORY);
    //~}

    /**
     * Logger for this class.
     */
    @UnknownNullability
    private static final Logger LOGGER = (Variables.DEBUG_LOGS ? LogManager.getLogger("HCsCR/Keys") : null);

    /**
     * An instance of this class cannot be created.
     *
     * @throws AssertionError Always
     * @deprecated Always throws
     */
    @ApiStatus.ScheduledForRemoval
    @Deprecated
    @Contract(value = "-> fail", pure = true)
    private Keys() {
        if (Variables.DEBUG_ASSERTS) {
            throw new AssertionError("HCsCR: No instances.");
        }
    }

    /**
     * Handles the keys. Should be called every tick from {@link HCsCR#tick(Minecraft)}.
     *
     * @param client   Client game instance
     * @param profiler Client profiler, {@code null} if {@link Variables#DEBUG_PROFILER} is {@code false}
     * @see HCsCR#tick(Minecraft)
     * @see #config(Minecraft, ProfilerFiller)
     * @see #toggle(Minecraft, ProfilerFiller)
     */
    public static void tick(final Minecraft client, final @UnknownNullability ProfilerFiller profiler) {
        // Validate.
        if (Variables.DEBUG_ASSERTS) {
            assert (client != null) : "HCsCR: Parameter 'client' is null. (profiler: " + profiler + ')';
            if (Variables.DEBUG_PROFILER) {
                assert (profiler != null) : "HCsCR: Parameter 'profiler' is null. (client: " + client + ')';
            }
            assert (client.isSameThread()) : "HCsCR: Wrong thread. (thread: " + Thread.currentThread() + ", client: " + client + ", profiler: " + profiler + ')';
        }

        // Push the profiler.
        if (Variables.DEBUG_PROFILER) {
            profiler.push("hcscr:keys"); // Implicit NPE for 'profiler'
        }

        // Delegate.
        config(client, profiler); // Implicit NPE for 'client'
        toggle(client, profiler); // Implicit NPE for 'client'

        // Pop the profiler.
        if (Variables.DEBUG_PROFILER) {
            profiler.pop();
        }
    }

    /**
     * Handles the {@link #CONFIG} key. Should be called every tick.
     *
     * @param client   Client game instance
     * @param profiler Client profiler, {@code null} if {@link Variables#DEBUG_PROFILER} is {@code false}
     * @see #CONFIG
     * @see #tick(Minecraft, ProfilerFiller)
     * @see #toggle(Minecraft, ProfilerFiller)
     */
    private static void config(final Minecraft client, final @UnknownNullability ProfilerFiller profiler) {
        // Validate.
        if (Variables.DEBUG_ASSERTS) {
            assert (client != null) : "HCsCR: Parameter 'client' is null. (profiler: " + profiler + ')';
            if (Variables.DEBUG_PROFILER) {
                assert (profiler != null) : "HCsCR: Parameter 'profiler' is null. (client: " + client + ')';
            }
            assert (client.isSameThread()) : "HCsCR: Wrong thread. (thread: " + Thread.currentThread() + ", client: " + client + ", profiler: " + profiler + ')';
        }

        // Push the profiler.
        if (Variables.DEBUG_PROFILER) {
            profiler.push("hcscr:keys/config"); // Implicit NPE for 'profiler'
        }

        // Consume the key.
        while (CONFIG.consumeClick()) {
            // Log. (**TRACE**)
            if (Variables.DEBUG_LOGS) {
                LOGGER.trace(HCsCR.MARKER, "HCsCR: Consuming config key... (client: {}, key: {})", client, CONFIG);
            }

            // Do nothing if some (another) screen is open.
            //? if >=26.2 {
            final Screen currentScreen = client.gui.screen(); // Implicit NPE for 'client'
            //?} else {
            /*final Screen currentScreen = client.screen; // Implicit NPE for 'client'
            *///?}
            if (currentScreen != null) {
                // Log. (**DEBUG**)
                if (Variables.DEBUG_LOGS) {
                    LOGGER.debug(HCsCR.MARKER, "HCsCR: Can't open config, some screen is open. (client: {}, currentScreen: {}, key: {})", client, currentScreen, CONFIG);
                }

                // Continue.
                continue;
            }

            // Open the config screen.
            final ConfigScreen screen = new ConfigScreen(null);
            //$ set_screen client screen
            client.gui.setScreen(screen);

            // Log. (**DEBUG**)
            if (Variables.DEBUG_LOGS) {
                LOGGER.debug(HCsCR.MARKER, "HCsCR: Opened the config. (client: {}, screen: {}, key: {})", client, screen, CONFIG);
            }
        }

        // Pop the profiler.
        if (Variables.DEBUG_PROFILER) {
            profiler.pop();
        }
    }

    /**
     * Handles the {@link #TOGGLE} key. Should be called every tick.
     *
     * @param client   Client game instance
     * @param profiler Client profiler, {@code null} if {@link Variables#DEBUG_PROFILER} is {@code false}
     * @see #TOGGLE
     * @see #tick(Minecraft, ProfilerFiller)
     * @see #config(Minecraft, ProfilerFiller)
     */
    private static void toggle(final Minecraft client, final @UnknownNullability ProfilerFiller profiler) {
        // Validate.
        if (Variables.DEBUG_ASSERTS) {
            assert (client != null) : "HCsCR: Parameter 'client' is null. (profiler: " + profiler + ')';
            if (Variables.DEBUG_PROFILER) {
                assert (profiler != null) : "HCsCR: Parameter 'profiler' is null. (client: " + client + ')';
            }
            assert (client.isSameThread()) : "HCsCR: Wrong thread. (thread: " + Thread.currentThread() + ", client: " + client + ", profiler: " + profiler + ')';
        }

        // Push the profiler.
        if (Variables.DEBUG_PROFILER) {
            profiler.push("hcscr:keys/toggle"); // Implicit NPE for 'profiler'
        }

        // Consume the key.
        while (TOGGLE.consumeClick()) {
            // Log. (**TRACE**)
            if (Variables.DEBUG_LOGS) {
                LOGGER.trace(HCsCR.MARKER, "HCsCR: Consuming toggle key... (client: {}, key: {})", client, TOGGLE);
            }

            // Toggle the mod.
            final boolean newState = Config.toggle();

            // Show the bar, play the sound.
            final Component message = HStonecutter.translate("hcscr." + newState)
                    .withStyle(newState ? ChatFormatting.GREEN : ChatFormatting.RED)
                    .withStyle(ChatFormatting.BOLD);
            //? if >=26.2 {
            client.gui.hud.setOverlayMessage(message, /*animate=*/false); // Implicit NPE for 'client'
            //?} else {
            /*client.gui.setOverlayMessage(message, /^animate=^/false); // Implicit NPE for 'client'
            *///?}
            client.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_PLING, (newState ? 2.0f : 0.0f)));

            // Log. (**DEBUG**)
            if (Variables.DEBUG_LOGS) {
                LOGGER.debug(HCsCR.MARKER, "HCsCR: Toggled the mod. (client: {}, newState: {}, key: {})", client, newState, TOGGLE);
            }
        }

        // Pop the profiler.
        if (Variables.DEBUG_PROFILER) {
            profiler.pop();
        }
    }
}
