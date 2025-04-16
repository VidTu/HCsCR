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
import net.minecraft.util.Mth;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * HCsCR config storage.
 *
 * @author VidTu
 */
@ApiStatus.Internal
@NullMarked
public final class HConfig {
    /**
     * Logger for this class.
     */
    public static final Logger LOGGER = LogManager.getLogger("HCsCR/HConfig");

    /**
     * Config GSON.
     */
    private static final Gson GSON = new GsonBuilder()
            .excludeFieldsWithModifiers(Modifier.TRANSIENT, Modifier.FINAL)
            .create();

    /**
     * Enable the mod, {@code true} by default.
     */
    public static boolean enable = true;

    /**
     * Crystals removal mode, {@link CrystalMode#DIRECT} by default.
     */
    public static CrystalMode crystals = CrystalMode.DIRECT;

    /**
     * Crystals removal delay in milliseconds, {@code 0} by default. Some users report that setting the delay
     * to the server's MSPT value actually makes crystal spamming a bit faster.
     */
    public static int crystalsDelay = 0;

    /**
     * Anchors removal mode, {@link AnchorMode#COLLISION} by default.
     */
    public static AnchorMode anchors = AnchorMode.COLLISION;

    /**
     * Anchors removal delay in milliseconds, {@code 0} by default.
     */
    public static int anchorsDelay = 0;

    /**
     * Creates a new config for GSON.
     */
    @Contract(pure = true)
    private HConfig() {
        // Private
    }

    /**
     * Loads the config, suppressing and logging any errors.
     *
     * @param directory Directory where all the configs are stored
     */
    public static void load(Path directory) {
        try {
            // Log. (**TRACE**)
            LOGGER.trace("HCsCR: Loading the config... (directory: {})", directory);

            // Resolve the file.
            Path file = directory.resolve("hcscr.json");

            // Read the config.
            try (BufferedReader reader = Files.newBufferedReader(file)) {
                GSON.fromJson(reader, HConfig.class);
            }

            // Log. (**DEBUG**)
            LOGGER.debug("HCsCR: Config has been loaded. (directory: {}, file: {})", directory, file);
        } catch (Throwable t) {
            // Log.
            LOGGER.error("HCsCR: Unable to load the HCsCR config.", t);
        } finally {
            // NPE protection.
            crystals = MoreObjects.firstNonNull(crystals, CrystalMode.DIRECT);
            crystalsDelay = Mth.clamp(crystalsDelay, 0, 200);
            anchors = MoreObjects.firstNonNull(anchors, AnchorMode.COLLISION);
            anchorsDelay = Mth.clamp(anchorsDelay, 0, 200);
        }
    }

    /**
     * Saves the config, suppressing and logging any errors.
     *
     * @param directory Directory where all the configs are stored
     */
    public static void saveOrLog(Path directory) {
        try {
            // Log. (**TRACE**)
            LOGGER.trace("HCsCR: Saving the config... (directory: {})", directory);

            // Resolve the file.
            Path file = directory.resolve("hcscr.json");

            // Write the config.
            Files.createDirectories(directory);
            try (BufferedWriter writer = Files.newBufferedWriter(file)) {
                //noinspection InstantiationOfUtilityClass
                GSON.toJson(new HConfig(), writer);
            }

            // Log. (**DEBUG**)
            LOGGER.debug("HCsCR: Config has been saved. (directory: {}, file: {})", directory, file);
        } catch (Throwable t) {
            // Log.
            LOGGER.error("HCsCR: Unable to save the HCsCR config.", t);
        }
    }
}
