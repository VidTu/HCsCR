/*
 * Copyright (c) 2023 Offenderify, VidTu
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

package ru.femboypve.hcscr;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Shared HCsCR class.
 *
 * @author Offenderify
 * @author VidTu
 */
public final class HCsCR {
    // Shared
    public static final Logger LOG = LoggerFactory.getLogger("HCsCR");
    public static final Gson GSON = new Gson();

    // Mod config
    public static boolean enabled = true;
    public static boolean removeCrystals = true;
    public static boolean removeSlimes = false;
    public static boolean removeInteractions = false;
    public static boolean removeAnchors = false;
    public static int delay = 0;
    public static boolean absolutePrecision = false;
    public static Batching batching = Batching.DISABLED;

    // Server config
    public static boolean serverDisabled;

    private HCsCR() {
        throw new AssertionError("No instances.");
    }

    /**
     * Loads mod config.
     *
     * @param path Config path
     */
    public static void loadConfig(Path path) {
        try {
            Path file = path.resolve("hcscr.json");
            if (!Files.isRegularFile(file)) return;
            JsonObject json = GSON.fromJson(Files.readString(file), JsonObject.class);
            enabled = json.has("enabled") ? json.get("enabled").getAsBoolean() : enabled;
            removeCrystals = json.has("removeCrystals") ? json.get("removeCrystals").getAsBoolean() : removeCrystals;
            removeSlimes = json.has("removeSlimes") ? json.get("removeSlimes").getAsBoolean() : removeSlimes;
            removeInteractions = json.has("removeInteractions") ? json.get("removeInteractions").getAsBoolean() : removeInteractions;
            removeAnchors = json.has("removeAnchors") ? json.get("removeAnchors").getAsBoolean() : removeAnchors;
            delay = json.has("delay") ? json.get("delay").getAsInt() : delay;
            absolutePrecision = json.has("absolutePrecision") ? json.get("absolutePrecision").getAsBoolean() : absolutePrecision;
            batching = json.has("batching") ? Objects.requireNonNullElse(Batching.byId(json.get("batching").getAsString()), batching) : batching;
        } catch (Exception e) {
            LOG.warn("Unable to load HCsCR config.", e);
        }
    }

    /**
     * Saves mod config.
     *
     * @param path Config path
     */
    public static void saveConfig(Path path) {
        try {
            Path file = path.resolve("hcscr.json");
            Files.createDirectories(file.getParent());
            JsonObject json = new JsonObject();
            json.addProperty("enabled", enabled);
            json.addProperty("removeCrystals", removeCrystals);
            json.addProperty("removeSlimes", removeSlimes);
            json.addProperty("removeInteractions", removeInteractions);
            json.addProperty("removeAnchors", removeAnchors);
            json.addProperty("delay", delay);
            json.addProperty("absolutePrecision", absolutePrecision);
            json.addProperty("batching", batching.id());
            Files.writeString(file, GSON.toJson(json));
        } catch (Exception e) {
            LOG.warn("Unable to save HCsCR config.", e);
        }
    }
}
