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
 */

package ru.vidtu.hcscr.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * HCsCR config.
 *
 * @author VidTu
 */
public final class HConfig {
    /**
     * Config GSON.
     */
    @NotNull
    private static final Gson GSON = new GsonBuilder()
            .excludeFieldsWithModifiers(Modifier.TRANSIENT, Modifier.FINAL)
            .create();

    /**
     * Logger for this class.
     */
    @NotNull
    public static final Logger LOGGER = LogManager.getLogger("HCsCR/HConfig");

    /**
     * Whether the mod is enabled, {@code true} by default.
     */
    public static boolean enabled = true;

    /**
     * Whether the crystals are removed on left click, {@code true} by default.
     */
    public static boolean removeCrystals = true;

    /**
     * Whether the invisible slimes and magma cubes are removed on left click, {@code false} by default.
     *
     * @apiNote Some servers use invisible slimes to prevent crystal optimizers from working
     */
    public static boolean removeSlimes = false;

    /**
     * Whether the "interaction" entities are removed on left click, {@code false} by default.
     *
     * @apiNote Some servers use interaction entities to prevent crystal optimizers from working
     * @implNote Works only on 1.19.4 and higher
     */
    public static boolean removeInteractions = false;

    /**
     * Whether the anchor blocks are removed on right click, {@code false} by default.
     *
     * @apiNote This will prevent the "air place" mechanic from working
     * @implNote Block removal is not affected by the {@link #delay}
     */
    public static boolean removeAnchors = false;

    /**
     * Entity removal delay in milliseconds, {@code 0} by default.
     * Set to {@code 0} to disable.
     *
     * @apiNote Some users report that setting this to server's MSPT value actually makes crystal spamming faster
     */
    public static int delay = 0;

    /**
     * Current batching mode, {@link Batching#DISABLED} by default.
     */
    public static Batching batching = Batching.DISABLED;

    /**
     * Creates a new config for GSON.
     */
    @Contract(pure = true)
    private HConfig() {
        // Private
    }

    /**
     * Loads the config, suppressing and logging any errors.
     */
    public static void loadOrLog() {
        try {
            // Log.
            Path path = FabricLoader.getInstance().getConfigDir();
            LOGGER.debug("HCsCR: Loading config for {}...", path);

            // Get the file.
            Path file = path.resolve("hcscr.json");

            // Skip if it doesn't exist.
            if (!Files.isRegularFile(file)) {
                LOGGER.debug("HCsCR: Config not found. Saving...");
                saveOrLog();
                return;
            }

            // Read the file.
            byte[] data = Files.readAllBytes(file);
            String value = new String(data, StandardCharsets.UTF_8);

            // Read JSON.
            JsonObject json = GSON.fromJson(value, JsonObject.class);

            // Hacky JSON reading.
            GSON.fromJson(json, HConfig.class);

            // Log it.
            LOGGER.debug("HCsCR: Config loaded.");
        } catch (Throwable t) {
            // Log.
            LOGGER.error("Unable to load HCsCR config.", t);
        } finally {
            // NPE protection.
            delay = Math.max(0, Math.min(200, delay));
            batching = (batching == null ? Batching.DISABLED : batching);
        }
    }

    /**
     * Saves the config, suppressing and logging any errors.
     */
    public static void saveOrLog() {
        try {
            // Log.
            Path path = FabricLoader.getInstance().getConfigDir();
            LOGGER.debug("HCsCR: Saving config into {}...", path);

            // NPE protection.
            delay = Math.max(0, Math.min(200, delay));
            batching = (batching == null ? Batching.DISABLED : batching);

            // Get the file.
            Path file = path.resolve("hcscr.json");

            // Hacky JSON writing.
            @SuppressWarnings("InstantiationOfUtilityClass") // <- Hack.
            JsonObject json = (JsonObject) GSON.toJsonTree(new HConfig());

            // Write JSON.
            String value = GSON.toJson(json);
            byte[] data = value.getBytes(StandardCharsets.UTF_8);

            // Create parent directories.
            Files.createDirectories(file.getParent());

            // Write the file.
            Files.write(file, data, StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE,
                    StandardOpenOption.SYNC, StandardOpenOption.DSYNC);

            // Log.
            LOGGER.debug("HCsCR: Config saved to {}.", file);
        } catch (Throwable t) {
            // Log.
            LOGGER.error("Unable to save HCsCR config.", t);
        }
    }
}
