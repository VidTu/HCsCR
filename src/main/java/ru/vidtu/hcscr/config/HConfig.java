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
import com.google.gson.annotations.SerializedName;
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
import ru.vidtu.hcscr.HCsCR;
import ru.vidtu.hcscr.platform.HCompile;
import ru.vidtu.hcscr.platform.HStonecutter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

//? if >=1.19.4 {
import net.minecraft.world.entity.Interaction;
//?}

/**
 * HCsCR config storage.
 *
 * @author VidTu
 * @apiNote Internal use only
 * @see HScreen
 */
@ApiStatus.Internal
@NullMarked
public final class HConfig {
    /**
     * Logger for this class.
     */
    private static final Logger LOGGER = LogManager.getLogger("HCsCR/HConfig");

    /**
     * GSON instance for configuration loading/saving.
     */
    private static final Gson GSON = new GsonBuilder()
            .excludeFieldsWithModifiers(Modifier.FINAL, Modifier.TRANSIENT)
            .create();

    /**
     * Enable the mod, {@code true} by default.
     */
    @SerializedName("enable")
    private static /*non-final*/ boolean enable = true;

    /**
     * Crystals removal mode, {@link CrystalMode#DIRECT} by default.
     *
     * @see #crystalsDelay
     * @see #crystalsResync
     */
    @SerializedName("crystals")
    private static /*non-final*/ CrystalMode crystals = CrystalMode.DIRECT;

    /**
     * Crystals removal delay in nanos, {@code 0} by default.
     * <p>
     * Some users report that setting the delay to the server's
     * MSPT value actually makes crystal spamming a bit faster.
     *
     * @see #crystals
     * @see #crystalsResync
     */
    @SerializedName("crystalsDelay")
    @Range(from = 0L, to = 200_000_000L)
    private static /*non-final*/ int crystalsDelay = 0;

    /**
     * Crystals resync delay in ticks, {@code 20} by default.
     * <p>
     * This is the delay after which the crystal will reappear again,
     * if the server hasn't actually removed it.
     * <p>
     * Useful to prevent ghost crystals, when a crystal
     * hit is not registered by the server.
     *
     * @see #crystals
     * @see #crystalsDelay
     */
    @SerializedName("crystalsResync")
    @Range(from = 0L, to = 50L)
    private static /*non-final*/ int crystalsResync = 20;

    /**
     * Blocks (anchors/beds) removal mode, {@link BlockMode#COLLISION} by default.
     */
    @SerializedName(value = "blocks", alternate = "anchors") // was "anchors" in HCsCR 2.0.2
    private static /*non-final*/ BlockMode blocks = BlockMode.COLLISION;

    /**
     * Creates a new config via GSON.
     */
    @Contract(pure = true)
    private HConfig() {
        // Empty.
    }

    /**
     * Loads the config, suppressing and logging any errors.
     *
     * @see #save()
     */
    public static void load() {
        try {
            // Log. (**TRACE**)
            if (HCompile.DEBUG_LOGS) {
                LOGGER.trace(HCsCR.HCSCR_MARKER, "HCsCR: Loading the config... (directory: {})", HStonecutter.CONFIG_DIRECTORY);
            }

            // Resolve the file.
            final Path file = HStonecutter.CONFIG_DIRECTORY.resolve("hcscr.json");

            // Read the config.
            try (final BufferedReader reader = Files.newBufferedReader(file)) {
                // Load the config.
                GSON.fromJson(reader, HConfig.class);
            }

            // Log. (**DEBUG**)
            if (HCompile.DEBUG_LOGS) {
                LOGGER.debug(HCsCR.HCSCR_MARKER, "HCsCR: Config has been loaded. (directory: {}, file: {})", HStonecutter.CONFIG_DIRECTORY, file);
            }
        } catch (final NoSuchFileException nsfe) {
            // Log. (**DEBUG**)
            if (HCompile.DEBUG_LOGS) {
                LOGGER.debug(HCsCR.HCSCR_MARKER, "HCsCR: Ignoring missing HCsCR config.", nsfe);
            }
        } catch (final Throwable t) {
            // Log.
            LOGGER.error(HCsCR.HCSCR_MARKER, "HCsCR: Unable to load the HCsCR config.", t);
        } finally {
            // Clamp.
            crystals = MoreObjects.firstNonNull(crystals, CrystalMode.DIRECT);
            crystalsDelay = Mth.clamp(((crystalsDelay / 1_000_000) * 1_000_000), 0, 200_000_000);
            crystalsResync = Mth.clamp(crystalsResync, 0, 50);
            blocks = MoreObjects.firstNonNull(blocks, BlockMode.COLLISION);
        }
    }

    /**
     * Saves the config, suppressing and logging any errors.
     *
     * @see #load()
     */
    /*package-private*/ static void save() {
        try {
            // Log. (**TRACE**)
            if (HCompile.DEBUG_LOGS) {
                LOGGER.trace(HCsCR.HCSCR_MARKER, "HCsCR: Saving the config... (directory: {})", HStonecutter.CONFIG_DIRECTORY);
            }

            // Resolve the file.
            final Path file = HStonecutter.CONFIG_DIRECTORY.resolve("hcscr.json");

            // Write the config.
            Files.createDirectories(file.getParent());
            try (final BufferedWriter writer = Files.newBufferedWriter(file)) {
                GSON.toJson(new HConfig(), writer);
            }

            // Log. (**DEBUG**)
            if (HCompile.DEBUG_LOGS) {
                LOGGER.debug(HCsCR.HCSCR_MARKER, "HCsCR: Config has been saved. (directory: {}, file: {})", HStonecutter.CONFIG_DIRECTORY, file);
            }
        } catch (final Throwable t) {
            // Log.
            LOGGER.error(HCsCR.HCSCR_MARKER, "HCsCR: Unable to save the HCsCR config.", t);
        }
    }

    @Contract(pure = true)
    @Override
    public String toString() {
        return "HCsCR/HConfig{}";
    }

    /**
     * Gets the enabled state.
     *
     * @return Enable the mod, {@code true} by default
     * @see #enable(boolean)
     * @see #toggle()
     */
    @Contract(pure = true)
    public static boolean enable() {
        return enable;
    }

    /**
     * Sets the enabled state.
     *
     * @param enable Enable the mod, {@code true} by default
     * @see #enable()
     * @see #toggle()
     */
    /*package-private*/ static void enable(final boolean enable) {
        HConfig.enable = enable;
    }

    /**
     * Gets the crystals.
     *
     * @return Crystals removal mode, {@link CrystalMode#DIRECT} by default.
     * @see #cycleCrystals(boolean)
     * @see #shouldProcess(Entity)
     * @see #crystalsDelay()
     * @see #crystalsResync()
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
     * @see #crystals()
     */
    @CheckReturnValue
    /*package-private*/ static CrystalMode cycleCrystals(final boolean back) {
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
     * @return Crystals removal delay in nanos, {@code 0} by default
     * @see #crystalsDelay(int)
     * @see #crystals()
     * @see #crystalsResync()
     */
    @Contract(pure = true)
    @Range(from = 0L, to = 200_000_000L)
    public static int crystalsDelay() {
        return crystalsDelay;
    }

    /**
     * Sets the crystals delay.
     *
     * @param crystalsDelay Crystals removal delay in nanos, {@code 0} by default
     * @see #crystalsDelay()
     */
    /*package-private*/ static void crystalsDelay(@Range(from = 0L, to = 200_000_000L) final int crystalsDelay) {
        HConfig.crystalsDelay = Mth.clamp(((crystalsDelay / 1_000_000) * 1_000_000), 0, 200_000_000);
    }

    /**
     * Gets the crystals resync.
     *
     * @return Crystals resync delay in ticks, {@code 20} by default
     * @see #crystalsResync(int)
     * @see #crystals()
     * @see #crystalsDelay()
     */
    @Contract(pure = true)
    @Range(from = 0L, to = 50L)
    public static int crystalsResync() {
        return crystalsResync;
    }

    /**
     * Sets the crystals resync.
     *
     * @param crystalsResync Crystals resync delay in ticks, {@code 20} by default
     * @see #crystalsResync()
     */
    /*package-private*/ static void crystalsResync(@Range(from = 0L, to = 50L) final int crystalsResync) {
        HConfig.crystalsResync = Mth.clamp(crystalsResync, 0, 50);
    }

    /**
     * Gets the blocks.
     *
     * @return Blocks removal mode, {@link BlockMode#COLLISION} by default
     * @see #cycleBlocks(boolean)
     */
    @Contract(pure = true)
    public static BlockMode blocks() {
        return blocks;
    }

    /**
     * Cycles the blocks.
     *
     * @param back Whether to cycle backwards
     * @return New blocks mode
     * @see #blocks()
     */
    @CheckReturnValue
    /*package-private*/ static BlockMode cycleBlocks(final boolean back) {
        switch (blocks) {
            case OFF: return (blocks = (back ? BlockMode.FULL : BlockMode.COLLISION));
            case COLLISION: return (blocks = (back ? BlockMode.OFF : BlockMode.FULL));
            case FULL: return (blocks = (back ? BlockMode.COLLISION : BlockMode.OFF));
            default: return (blocks = BlockMode.COLLISION);
        }
    }

    /**
     * Checks whether the entity should be processed. This doesn't check the {@link #enable()} state.
     *
     * @param entity Entity to check
     * @return Whether the entity should be processed
     * @see #crystals()
     */
    @Contract(pure = true)
    public static boolean shouldProcess(final Entity entity) {
        // Validate.
        if (HCompile.DEBUG_ASSERTS) {
            assert (entity != null) : "HCsCR: Parameter 'entity' is null.";
        }

        // Check depending on the mode.
        switch (crystals) {
            case DIRECT:
                return (entity instanceof EndCrystal);
            case ENVELOPING:
                //? if >=1.19.4 {
                //noinspection SimplifiableIfStatement // <- Preprocessor Statement.
                if (entity instanceof Interaction) return true;
                //?}
                return ((entity instanceof EndCrystal) || ((entity instanceof Slime) && entity.isInvisible()));
            default:
                return false;
        }
    }

    /**
     * Toggles the current config enable state and saves the config.
     *
     * @return New (current) enabled state
     * @see #enable()
     * @see #enable(boolean)
     * @see #save()
     */
    @CheckReturnValue
    public static boolean toggle() {
        final boolean newState = (enable = !enable);
        save();
        return newState;
    }
}
