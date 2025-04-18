/*
 * Copyright (c) 2023 Offenderify
 * Copyright (c) 2023-2025 VidTu
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

package ru.vidtu.hcscr.config;

import com.google.common.base.MoreObjects;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.monster.Slime;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Range;
import org.jspecify.annotations.NullMarked;
import ru.vidtu.hcscr.platform.HStonecutter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * HCsCR config storage.
 *
 * @author VidTu
 * @apiNote Internal use only
 */
@ApiStatus.Internal
@NullMarked
public final class HConfig implements Cloneable {
    /**
     * Logger for this class.
     */
    private static final Logger LOGGER = LogManager.getLogger("HCsCR/HConfig");

    /**
     * GSON instance for configuration loading/saving.
     */
    private static final Gson GSON = new GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .create();

    /**
     * Enable the mod, {@code true} by default.
     */
    @Expose
    private static boolean enable = true;

    /**
     * Crystals removal mode, {@link CrystalMode#DIRECT} by default.
     */
    @Expose
    private static CrystalMode crystals = CrystalMode.DIRECT;

    /**
     * Crystals removal delay in milliseconds, {@code 0} by default. Some users report that setting the delay
     * to the server's MSPT value actually makes crystal spamming a bit faster.
     *
     * @see #crystalsDelayNanos
     */
    @Expose
    @Range(from = 0L, to = 200L)
    private static int crystalsDelay = 0;

    /**
     * A projection of {@link #crystalsDelay} in nanos.
     *
     * @see #crystalsDelay
     */
    private static long crystalsDelayNanos = 0;

    /**
     * Crystals resync delay in ticks, {@code 20} by default. This is the delay after which the crystal
     * will reappear again, if the server hasn't actually removed it. Useful to prevent ghost crystals,
     * when a crystal hit is not registered by the server.
     */
    @Expose
    @Range(from = 0L, to = 50L)
    private static int crystalsResync = 20;

    /**
     * Anchors removal mode, {@link AnchorMode#COLLISION} by default.
     */
    @Expose
    private static AnchorMode anchors = AnchorMode.COLLISION;

    /**
     * Anchors removal delay in milliseconds, {@code 0} by default.
     */
    @Expose
    @Range(from = 0L, to = 200L)
    private static int anchorsDelay = 0;

    /**
     * Creates a new config via GSON.
     */
    @Contract(pure = true)
    private HConfig() {
        // Private
    }

    /**
     * Loads the config, suppressing and logging any errors.
     */
    public static void load() {
        try {
            // Log. (**TRACE**)
            LOGGER.trace("HCsCR: Loading the config... (directory: {})", HStonecutter.CONFIG_DIRECTORY);

            // Resolve the file.
            Path file = HStonecutter.CONFIG_DIRECTORY.resolve("hcscr.json");

            // Read the config.
            try (BufferedReader reader = Files.newBufferedReader(file)) {
                // Load the config.
                GSON.fromJson(reader, HConfig.class);
            }

            // Log. (**DEBUG**)
            LOGGER.debug("HCsCR: Config has been loaded. (directory: {}, file: {})", HStonecutter.CONFIG_DIRECTORY, file);
        } catch (Throwable t) {
            // Log.
            LOGGER.error("HCsCR: Unable to load the HCsCR config.", t);
        } finally {
            // Clamp.
            crystals = MoreObjects.firstNonNull(crystals, CrystalMode.DIRECT);
            crystalsDelay = Mth.clamp(crystalsDelay, 0, 200);
            anchors = MoreObjects.firstNonNull(anchors, AnchorMode.COLLISION);
            anchorsDelay = Mth.clamp(anchorsDelay, 0, 200);

            // Calculate.
            crystalsDelayNanos = (crystalsDelay * 1_000_000L);
        }
    }

    /**
     * Saves the config, suppressing and logging any errors.
     */
    static void save() {
        try {
            // Log. (**TRACE**)
            LOGGER.trace("HCsCR: Saving the config... (directory: {})", HStonecutter.CONFIG_DIRECTORY);

            // Resolve the file.
            Path file = HStonecutter.CONFIG_DIRECTORY.resolve("hcscr.json");

            // Write the config.
            Files.createDirectories(file.getParent());
            try (BufferedWriter writer = Files.newBufferedWriter(file)) {
                GSON.toJson(new HConfig(), writer);
            }

            // Log. (**DEBUG**)
            LOGGER.debug("HCsCR: Config has been saved. (directory: {}, file: {})", HStonecutter.CONFIG_DIRECTORY, file);
        } catch (Throwable t) {
            // Log.
            LOGGER.error("HCsCR: Unable to save the HCsCR config.", t);
        }
    }

    /**
     * Gets the enabled state.
     *
     * @return Enable the mod, {@code true} by default
     */
    @Contract(pure = true)
    public static boolean enable() {
        return enable;
    }

    /**
     * Sets the enabled state.
     *
     * @param enable Enable the mod, {@code true} by default
     */
    static void enable(boolean enable) {
        HConfig.enable = enable;
    }

    /**
     * Gets the crystals.
     *
     * @return Crystals removal mode, {@link CrystalMode#DIRECT} by default.
     */
    @Contract(pure = true)
    public static CrystalMode crystals() {
        return crystals;
    }

    /**
     * Cycles the crystals.
     *
     * @param back Whether to cycle backwards
     * @return New crystal mode
     */
    @CheckReturnValue
    static CrystalMode cycleCrystals(boolean back) {
        switch (crystals) {
            case OFF: return (crystals = (back ? CrystalMode.ENVELOPING : CrystalMode.DIRECT));
            case DIRECT: return (crystals = (back ? CrystalMode.OFF : CrystalMode.ENVELOPING));
            case ENVELOPING: return (crystals = (back ? CrystalMode.DIRECT : CrystalMode.OFF));
            default: return (crystals = CrystalMode.DIRECT);
        }
    }

    /**
     * Gets the crystals delay.
     *
     * @return Crystals removal delay in milliseconds, {@code 0} by default
     */
    @Contract(pure = true)
    @Range(from = 0L, to = 200L)
    static int crystalsDelay() {
        return crystalsDelay;
    }

    /**
     * Gets the {@link #crystalsDelay()} in nanos.
     *
     * @return Crystals removal delay in nanos, {@code 0} by default
     * @see #crystalsDelay()
     */
    @Contract(pure = true)
    public static long crystalsDelayNanos() {
        return crystalsDelayNanos;
    }

    /**
     * Sets the crystals delay.
     *
     * @param crystalsDelay Crystals removal delay in milliseconds, {@code 0} by default
     */
    static void crystalsDelay(@Range(from = 0L, to = 200L) int crystalsDelay) {
        HConfig.crystalsDelay = Mth.clamp(crystalsDelay, 0, 200);
        crystalsDelayNanos = Mth.clamp(crystalsDelay * 1_000_000L, 0, 200_000_000L);
    }

    /**
     * Gets the crystals resync.
     *
     * @return Crystals resync delay in ticks, {@code 20} by default
     */
    @Contract(pure = true)
    @Range(from = 0L, to = 50)
    public static int crystalsResync() {
        return crystalsResync;
    }

    /**
     * Sets the crystals resync.
     *
     * @param crystalsResync Crystals resync delay in ticks, {@code 20} by default
     */
    static void crystalsResync(@Range(from = 0L, to = 50) int crystalsResync) {
        HConfig.crystalsResync = Mth.clamp(crystalsResync, 0, 50);
    }

    /**
     * Gets the anchors.
     *
     * @return Anchors removal mode, {@link AnchorMode#COLLISION} by default
     */
    @Contract(pure = true)
    static AnchorMode anchors() {
        return anchors;
    }

    /**
     * Cycles the anchors.
     *
     * @param back Whether to cycle backwards
     * @return New anchors mode
     */
    @CheckReturnValue
    static AnchorMode cycleAnchors(boolean back) {
        switch (anchors) {
            case OFF: return (anchors = (back ? AnchorMode.FULL : AnchorMode.COLLISION));
            case COLLISION: return (anchors = (back ? AnchorMode.OFF : AnchorMode.FULL));
            case FULL: return (anchors = (back ? AnchorMode.COLLISION : AnchorMode.OFF));
            default: return (anchors = AnchorMode.COLLISION);
        }
    }

    /**
     * Gets the anchors delay.
     *
     * @return Anchors removal delay in milliseconds, {@code 0} by default
     */
    @Contract(pure = true)
    @Range(from = 0L, to = 200L)
    static int anchorsDelay() {
        return anchorsDelay;
    }

    /**
     * Sets the anchors delay.
     *
     * @param anchorsDelay Anchors removal delay in milliseconds, {@code 0} by default
     */
    static void anchorsDelay(@Range(from = 0L, to = 200L) int anchorsDelay) {
        HConfig.anchorsDelay = Mth.clamp(anchorsDelay, 0, 200);
    }

    /**
     * Checks whether the entity should be processed. This doesn't check the {@link #enable()} state.
     *
     * @param entity Entity to check
     * @return Whether the entity should be processed
     */
    @Contract(pure = true)
    public static boolean shouldProcess(Entity entity) {
        // Validate.
        assert entity != null : "Parameter 'entity' is null.";

        // Fast way for disabled mod.
        switch (crystals) {
            case DIRECT:
                return entity instanceof EndCrystal;
            case ENVELOPING:
                //? if >=1.19.4
                if (entity instanceof net.minecraft.world.entity.Interaction) return true;
                return entity instanceof EndCrystal || (entity instanceof Slime && entity.isInvisible());
            default:
                return false;
        }
    }

    /**
     * Gets whether hitting the interactions should be allowed and the mod is {@link #enable()}.
     *
     * @return Whether hitting the interaction entities is allowed by the current config
     */
    @Contract(pure = true)
    public static boolean allowHittingInteractions() {
        return enable && (crystals == CrystalMode.ENVELOPING);
    }

    /**
     * Toggles the current config enable state.
     *
     * @return New (current) enabled state
     */
    @CheckReturnValue
    public static boolean toggle() {
        boolean newState = (enable = !enable);
        save();
        return newState;
    }
}
